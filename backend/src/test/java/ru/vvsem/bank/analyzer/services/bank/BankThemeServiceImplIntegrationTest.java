package ru.vvsem.bank.analyzer.services.bank;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import ru.vvsem.bank.analyzer.configs.theme.BankThemeConfig;
import ru.vvsem.bank.analyzer.dto.BankThemeDto;
import ru.vvsem.bank.analyzer.mappers.BankThemeMapperImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {BankThemeConfig.class, BankThemeMapperImpl.class})
@EnableConfigurationProperties
@TestPropertySource(properties = {
        "colors.bank.themes.sber.primary-color=#1B5AA0",
        "colors.bank.themes.sber.secondary-color=#4CAF50",
        "colors.bank.themes.sber.text-color=#FFFFFF",
        "colors.bank.themes.tinkoff.name=Тинькофф",
        "colors.bank.themes.tinkoff.primary-color=#FFDD2D",
        "colors.bank.themes.tinkoff.secondary-color=#000000",
        "colors.bank.themes.tinkoff.text-color=#FFFFFF",
        "colors.bank.themes.default.primary-color=#666666",
        "colors.bank.themes.default.secondary-color=#999999",
        "colors.bank.themes.default.text-color=#999999"

})
@Import(BankThemeServiceImpl.class)
class BankThemeServiceImplIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private BankThemeService bankThemeService;

    @Test
    @DisplayName("Должен вернуть тему для существующего банка")
    void shouldReturnThemeForExistingBank() {
        // When
        BankThemeDto result = bankThemeService.getBankTheme("sber");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrimaryColor()).isEqualTo("#1B5AA0");
        assertThat(result.getSecondaryColor()).isEqualTo("#4CAF50");
    }

    @Test
    @DisplayName("Должен вернуть тему для другого существующего банка")
    void shouldReturnThemeForAnotherExistingBank() {
        // When
        BankThemeDto result = bankThemeService.getBankTheme("tinkoff");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrimaryColor()).isEqualTo("#FFDD2D");
        assertThat(result.getSecondaryColor()).isEqualTo("#000000");
    }

    @Test
    @DisplayName("Должен вернуть тему по умолчанию для несуществующего банка")
    void shouldReturnDefaultThemeForNonExistingBank() {
        // When
        BankThemeDto result = bankThemeService.getBankTheme("unknown-bank");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrimaryColor()).isEqualTo("#666666");
        assertThat(result.getSecondaryColor()).isEqualTo("#999999");
    }

    @Test
    @DisplayName("Должен вернуть тему по умолчанию когда bankCode равен null")
    void shouldReturnDefaultThemeWhenBankCodeIsNull() {
        // When
        BankThemeDto result = bankThemeService.getBankTheme(null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrimaryColor()).isEqualTo("#666666");
        assertThat(result.getSecondaryColor()).isEqualTo("#999999");
    }

    @Test
    @DisplayName("Должен вернуть тему по умолчанию когда bankCode пустой")
    void shouldReturnDefaultThemeWhenBankCodeIsEmpty() {
        // When
        BankThemeDto result = bankThemeService.getBankTheme("");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPrimaryColor()).isEqualTo("#666666");
        assertThat(result.getSecondaryColor()).isEqualTo("#999999");
    }

}