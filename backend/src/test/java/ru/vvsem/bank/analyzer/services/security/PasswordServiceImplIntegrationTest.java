package ru.vvsem.bank.analyzer.services.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
        PasswordServiceImpl.class,
        BCryptPasswordEncoder.class})
class PasswordServiceImplIntegrationTest {

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Должен закодировать пароль")
    void shouldEncodePassword() {
        // Given
        String rawPassword = "mySecurePassword123";

        // When
        String encodedPassword = passwordService.encodePassword(rawPassword);

        // Then
        assertThat(encodedPassword).isNotBlank();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt prefix
    }

    @Test
    @DisplayName("Должен вернуть true при совпадении паролей")
    void shouldReturnTrueWhenPasswordsMatch() {
        // Given
        String rawPassword = "testPassword";
        String encodedPassword = passwordService.encodePassword(rawPassword);

        // When
        boolean result = passwordService.matches(rawPassword, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false при несовпадении паролей")
    void shouldReturnFalseWhenPasswordsDoNotMatch() {
        // Given
        String rawPassword = "testPassword";
        String wrongPassword = "wrongPassword";
        String encodedPassword = passwordService.encodePassword(rawPassword);

        // When
        boolean result = passwordService.matches(wrongPassword, encodedPassword);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустой пароль")
    void shouldHandleEmptyPassword() {
        // Given
        String emptyPassword = "";

        // When
        String encodedPassword = passwordService.encodePassword(emptyPassword);
        boolean result = passwordService.matches(emptyPassword, encodedPassword);

        // Then
        assertThat(encodedPassword).isNotBlank();
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен корректно обрабатывать специальные символы в пароле")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String passwordWithSpecialChars = "P@ssw0rd!№%:;()_+-=[]{}";

        // When
        String encodedPassword = passwordService.encodePassword(passwordWithSpecialChars);
        boolean result = passwordService.matches(passwordWithSpecialChars, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен корректно обрабатывать длинные пароли")
    void shouldHandleLongPasswords() {
        // Given
        String longPassword = "a".repeat(72);

        // When
        String encodedPassword = passwordService.encodePassword(longPassword);
        boolean result = passwordService.matches(longPassword, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен генерировать разные хеши для одного и того же пароля")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        String rawPassword = "samePassword";

        // When
        String encodedPassword1 = passwordService.encodePassword(rawPassword);
        String encodedPassword2 = passwordService.encodePassword(rawPassword);

        // Then
        assertThat(encodedPassword1).isNotEqualTo(encodedPassword2);

        // Но оба должны верифицироваться корректно
        assertThat(passwordService.matches(rawPassword, encodedPassword1)).isTrue();
        assertThat(passwordService.matches(rawPassword, encodedPassword2)).isTrue();
    }

    @Test
    @DisplayName("Должен корректно работать с null паролем")
    void shouldHandleNullPassword() {
        // Given
        String nullPassword = null;

        // When & Then
        // encodePassword с null должен выбрасывать исключение
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.encodePassword(nullPassword)
        );

        // matches с null encodedPassword
        boolean result = passwordService.matches("anyPassword", null);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Должен корректно работать с очень короткими паролями")
    void shouldHandleVeryShortPasswords() {
        // Given
        String shortPassword = "a";

        // When
        String encodedPassword = passwordService.encodePassword(shortPassword);
        boolean result = passwordService.matches(shortPassword, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен корректно работать с паролями содержащими пробелы")
    void shouldHandlePasswordsWithSpaces() {
        // Given
        String passwordWithSpaces = "password with spaces";

        // When
        String encodedPassword = passwordService.encodePassword(passwordWithSpaces);
        boolean result = passwordService.matches(passwordWithSpaces, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен корректно работать с Unicode символами")
    void shouldHandleUnicodeCharacters() {
        // Given
        String unicodePassword = "парольСрусскимиСимволами123!@#";

        // When
        String encodedPassword = passwordService.encodePassword(unicodePassword);
        boolean result = passwordService.matches(unicodePassword, encodedPassword);

        // Then
        assertThat(result).isTrue();
    }
}