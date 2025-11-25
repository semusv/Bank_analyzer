package ru.vvsem.bank.analyzer.services.help;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vvsem.bank.analyzer.components.readers.ClassPathFileReader;
import ru.vvsem.bank.analyzer.exceptions.BusinessException;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {HelpServiceImpl.class})
class HelpServiceTest {

    @MockitoBean
    private ClassPathFileReader fileReader;

    @Autowired
    private HelpServiceImpl helpService;

    @Test
    @DisplayName("Должен успешно вернуть содержимое файла")
    void shouldReturnFileContent() {
        // Given
        String fileName = "README.md";
        String expectedContent = "File content";

        when(fileReader.fileExists(fileName)).thenReturn(true);
        when(fileReader.readFile(fileName)).thenReturn(expectedContent);

        // When
        String actualContent = helpService.getReadme(fileName);

        // Then
        assertThat(actualContent).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("Должен выбросить EntityNotFoundException, если файл отсутствует")
    void shouldThrowEntityNotFoundWhenFileMissing() {
        // Given
        String fileName = "nonexistent.md";
        when(fileReader.fileExists(fileName)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> helpService.getReadme(fileName))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("File not found: " + fileName)
                .hasFieldOrPropertyWithValue("messageCode", "exception.entity.not.found.readme");
    }

    @Test
    @DisplayName("Должен выбросить BusinessException при ошибке чтения файла")
    void shouldThrowBusinessExceptionWhenReadErrorOccurs() {
        // Given
        String fileName = "README.md";
        when(fileReader.fileExists(fileName)).thenReturn(true);
        when(fileReader.readFile(fileName))
                .thenThrow(new BusinessException(
                        "Can't read file: " + fileName,
                        "business.file.cant.read",
                        Map.of("filePath", fileName)
                ));

        // When & Then
        assertThatThrownBy(() -> helpService.getReadme(fileName))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Can't read file: " + fileName)
                .hasFieldOrPropertyWithValue("messageCode", "business.file.cant.read");
    }
}