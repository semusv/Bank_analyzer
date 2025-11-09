package ru.vvsem.bank.analyzer.repositories;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class BaseRepositoryTest {

    private static final PostgreSQLContainer<?> postgres;

    static {

        //noinspection resource
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
                .withDatabaseName("bank_analyzer_test")
                .withUsername("test")
                .withPassword("test")
                .withNetworkMode("bank_analyzer-network")
                .withNetworkAliases("testcontainers-db")
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

        // Важно: отключить проверку соединения при валидации, т.к. контейнер может "перезагрузиться"
        registry.add("spring.datasource.hikari.validation-timeout", () -> "3000");
        registry.add("spring.datasource.hikari.initialization-fail-timeout", () -> "3000");
    }
}