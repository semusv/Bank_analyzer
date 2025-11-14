package ru.vvsem.bank.analyzer.services.help;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import ru.vvsem.bank.analyzer.components.readers.ClassPathFileReader;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({
        HelpServiceImpl.class,
        ClassPathFileReader.class
})
class HelpServiceIntegrationTest {

    @Autowired
    private HelpService helpService;

    @Autowired
    private ClassPathFileReader classPathFileReader;

    @BeforeEach
    void setUp() {
        // Убедимся, что README.md существует в classpath
        ClassPathResource resource = new ClassPathResource("README.md");
        if (!resource.exists()) {
            throw new IllegalStateException("Test resource README.md not found in classpath");
        }
    }

    @Test
    @DisplayName("Должен успешно прочитать README.md и вернуть его содержимое")
    void shouldReadReadmeFileSuccessfully() throws IOException {
        // Given
        ClassPathResource resource = new ClassPathResource("README.md");
        String expectedContent;
        try (Scanner scanner = new Scanner(resource.getInputStream(), StandardCharsets.UTF_8)) {
            expectedContent = scanner.useDelimiter("\\A").next();
        }

        // When
        String actualContent = helpService.getReadme("README.md");

        // Then
        assertThat(actualContent).isNotEmpty();
        assertThat(actualContent.trim()).isEqualTo(expectedContent.trim());
        assertThat(actualContent).contains("Какой-то контент");
    }

    @Test
    @DisplayName("Должен выбросить EntityNotFoundException, если README.md отсутствует")
    void shouldThrowEntityNotFoundWhenReadmeMissing() {

        // When & Then
        assertThatThrownBy(() -> helpService.getReadme("nonexistentReadme.md"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("File not found");
    }

}