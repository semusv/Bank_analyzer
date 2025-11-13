package ru.vvsem.bank.analyzer.repositories;

import jakarta.persistence.Transient;
import org.hibernate.collection.spi.PersistentCollection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class BaseRepositoryTest {

    private static final PostgreSQLContainer<?> postgres;

    static {

        //noinspection resource
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                .withDatabaseName("bank_analyzer_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        postgres.start();

        printConnectionInfo();
    }

    public static void printConnectionInfo() {
        System.out.println("=== PGAdmin Connection Info ===");
        System.out.println("Host: " + postgres.getHost());
        System.out.println("Port: " + postgres.getMappedPort(5432));
        System.out.println("Database: " + postgres.getDatabaseName());
        System.out.println("Username: " + postgres.getUsername());
        System.out.println("Password: " + postgres.getPassword());
        System.out.println("JDBC URL: " + postgres.getJdbcUrl());
        System.out.println("===============================");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Оптимизация HikariCP для тестов
        registry.add("spring.datasource.hikari.connection-timeout", () -> "10000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "30000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "10000");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "1");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");

        registry.add("spring.datasource.hikari.validation-timeout", () -> "3000");
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> "3000");
    }

    protected void assertAllFieldsInitialized(Object dto, String... ignoredFieldsArgs) {
        if (dto == null) {
            throw new AssertionError("DTO is null");
        }

        List<String> ignoredFields = Arrays.asList(ignoredFieldsArgs);
        Class<?> clazz = dto.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (shouldSkipField(field, ignoredFields)) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(dto);

                // Проверяем, что поле не null
                assertThat(value)
                        .withFailMessage(() -> "Поле '%s' в DTO '%s' не проинициализировано (null)"
                                .formatted(field.getName(), dto.getClass().getSimpleName()))
                        .isNotNull();

                checkForLazyInitialization(field, value);

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Нет доступа к полю: " + field.getName(), e);
            }
        }
    }

    private boolean shouldSkipField(Field field, List<String> ignoredFields) {
        return field.getName().equals("serialVersionUID")
               || java.lang.reflect.Modifier.isStatic(field.getModifiers())
               || field.isAnnotationPresent(Transient.class)
               || ignoredFields.contains(field.getName());
    }

    private void checkForLazyInitialization(Field field, Object value) {
        // Проверка коллекций
        if (value instanceof Collection<?> collection) {
            assertThatCode(collection::size)
                    .withFailMessage(() -> "LazyInitializationException в коллекции '%s'".formatted(field.getName()))
                    .doesNotThrowAnyException();

            // Дополнительная проверка для Hibernate коллекций
            if (value instanceof PersistentCollection) {
                assertThat(((PersistentCollection<?>) value).wasInitialized())
                        .withFailMessage(() -> "Ленивая коллекция '%s' не инициализирована".formatted(field.getName()))
                        .isTrue();
            }
        }

        // Проверка массивов (с защитой от NPE)
        else if (value != null && value.getClass().isArray()) {
            assertThatCode(() -> java.lang.reflect.Array.getLength(value))
                    .withFailMessage(() -> "Ошибка доступа к массиву '%s'".formatted(field.getName()))
                    .doesNotThrowAnyException();
        }

        // Проверка Map
        else if (value instanceof java.util.Map<?, ?> map) {
            assertThatCode(map::size)
                    .withFailMessage(() -> "LazyInitializationException в Map '%s'".formatted(field.getName()))
                    .doesNotThrowAnyException();
        }

        // Проверка Optional (распаковываем и проверяем содержимое)
        else if (value instanceof java.util.Optional<?> optional) {
            optional.ifPresent(object -> checkForLazyInitialization(field, object));
        }

        // Рекурсивная проверка вложенных DTO (опционально)
        else if (isCustomDto(value)) {
            assertAllFieldsInitialized(value);
        }
    }

    private boolean isCustomDto(Object obj) {
        if (obj == null) return false;

        // Исключаем стандартные типы
        return !(obj instanceof String ||
                 obj instanceof Number ||
                 obj instanceof Boolean ||
                 obj instanceof java.util.Date ||
                 obj instanceof java.time.temporal.Temporal ||
                 obj instanceof Enum);
    }
}