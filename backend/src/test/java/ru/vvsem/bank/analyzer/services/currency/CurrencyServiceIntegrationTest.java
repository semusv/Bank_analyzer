package ru.vvsem.bank.analyzer.services.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapperImpl;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application.yml")
@Import({
        CurrencyServiceImpl.class,
        CurrencyMapperImpl.class
})
class CurrencyServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TestEntityManager entityManager;

    private Currency currencyRub;
    private Currency currencyUsd;
    private Currency currencyEur;

    @BeforeEach
    void setUp() {
        // Создаём валюты
        currencyRub = new Currency("RUB", "₽", "Russian Ruble");
        currencyUsd = new Currency("USD", "$", "US Dollar");
        currencyEur = new Currency("EUR", "€", "Euro");

        entityManager.persistAndFlush(currencyRub);
        entityManager.persistAndFlush(currencyUsd);
        entityManager.persistAndFlush(currencyEur);
    }

    @Test
    @DisplayName("Должен вернуть все валюты")
    void shouldReturnAllCurrencies() {
        // When
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(CurrencyDto::getCode)
                .containsExactlyInAnyOrder("RUB", "USD", "EUR");
    }

    @Test
    @DisplayName("Должен вернуть пустой список когда валют нет")
    void shouldReturnEmptyListWhenNoCurrencies() {
        // Given
        entityManager.clear();
        // Удаляем все валюты
        entityManager.getEntityManager().createQuery("DELETE FROM Currency").executeUpdate();

        // When
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно маппить сущности в DTO")
    void shouldCorrectlyMapEntitiesToDto() {
        // When
        List<CurrencyDto> result = currencyService.getAllCurrencies();

        // Then
        CurrencyDto rubDto = result.stream()
                .filter(dto -> "RUB".equals(dto.getCode()))
                .findFirst()
                .orElseThrow();

        assertThat(rubDto.getCode()).isEqualTo("RUB");
        assertThat(rubDto.getSymbol()).isEqualTo("₽");
        assertThat(rubDto.getName()).isEqualTo("Russian Ruble");
    }
}