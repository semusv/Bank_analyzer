package ru.vvsem.bank.analyzer.services.exchange_rate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.ExchangeRate;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.ExchangeRateRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application-test.yml")
@Import(ExchangeRateServiceImpl.class)
class ExchangeRateServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private org.springframework.web.servlet.RequestToViewNameTranslator requestToViewNameTranslator;

    private ExchangeRate usdRateToday;
    private ExchangeRate usdRateYesterday;
    private ExchangeRate eurRateToday;

    @BeforeEach
    void setUp() {
        // Создаём курсы валют
        usdRateToday = new ExchangeRate();
        usdRateToday.setCurrencyCode("USD");
        usdRateToday.setCurrencyDate(LocalDate.now());
        usdRateToday.setCurrencyName("US Dollar");
        usdRateToday.setNominal(1);
        usdRateToday.setValue(BigDecimal.valueOf(80));
        usdRateToday.setVunitRate(BigDecimal.valueOf(80));
        entityManager.persistAndFlush(usdRateToday);

        usdRateYesterday = new ExchangeRate();
        usdRateYesterday.setCurrencyCode("USD");
        usdRateYesterday.setCurrencyDate(LocalDate.now().minusDays(1));
        usdRateYesterday.setCurrencyName("US Dollar");
        usdRateYesterday.setNominal(1);
        usdRateYesterday.setValue(BigDecimal.valueOf(90));
        usdRateYesterday.setVunitRate(BigDecimal.valueOf(90));
        entityManager.persistAndFlush(usdRateYesterday);

        eurRateToday = new ExchangeRate();
        eurRateToday.setCurrencyCode("EUR");
        eurRateToday.setCurrencyDate(LocalDate.now());
        eurRateToday.setCurrencyName("Euro");
        eurRateToday.setNominal(1);
        eurRateToday.setValue(BigDecimal.valueOf(100));
        eurRateToday.setVunitRate(BigDecimal.valueOf(100));
        entityManager.persistAndFlush(eurRateToday);
    }

    @Test
    @DisplayName("Должен конвертировать RUB в RUB без изменений")
    void shouldConvertRubToRubWithoutChanges() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(1000);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "RUB");

        // Then
        assertThat(result).isEqualByComparingTo(amount);
    }

    @Test
    @DisplayName("Должен конвертировать USD в RUB")
    void shouldConvertUsdToRub() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "USD");

        // Then
        BigDecimal expected = amount.multiply(usdRateToday.getValue())
                .divide(BigDecimal.valueOf(usdRateToday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен конвертировать EUR в RUB")
    void shouldConvertEurToRub() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(50);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "EUR");

        // Then
        BigDecimal expected = amount.multiply(eurRateToday.getValue())
                .divide(BigDecimal.valueOf(eurRateToday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен конвертировать USD в RUB с номиналом больше 1")
    void shouldConvertUsdToRubWithNominal() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        ExchangeRate rateWithNominal = new ExchangeRate();
        rateWithNominal.setCurrencyCode("USD");
        rateWithNominal.setCurrencyDate(LocalDate.now().plusDays(1));
        rateWithNominal.setCurrencyName("US Dollar");
        rateWithNominal.setNominal(10); // 10 USD = 905 RUB
        rateWithNominal.setValue(BigDecimal.valueOf(905.0));
        rateWithNominal.setVunitRate(BigDecimal.valueOf(90.50));
        entityManager.persistAndFlush(rateWithNominal);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "USD", LocalDate.now().plusDays(1));

        // Then
        BigDecimal expected = amount.multiply(rateWithNominal.getValue())
                .divide(BigDecimal.valueOf(rateWithNominal.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен бросить исключение при отсутствии курса валюты")
    void shouldThrowExceptionWhenRateNotFound() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        String unknownCurrency = "CNY";

        // When & Then
        assertThatThrownBy(() -> exchangeRateService.convertToRub(amount, unknownCurrency))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Rate for code " + unknownCurrency);
    }

    @Test
    @DisplayName("Должен конвертировать список валют в RUB")
    void shouldConvertListToRub() {
        // Given
        List<CurrencyAmountDto> amounts = List.of(
                new CurrencyAmountDto("RUB", BigDecimal.valueOf(1000)),
                new CurrencyAmountDto("USD", BigDecimal.valueOf(100)),
                new CurrencyAmountDto("EUR", BigDecimal.valueOf(50))
        );

        // When
        BigDecimal result = exchangeRateService.convertListToRub(amounts);

        // Then
        BigDecimal rubAmount = BigDecimal.valueOf(1000);
        BigDecimal usdAmount = BigDecimal.valueOf(100).multiply(usdRateToday.getValue())
                .divide(BigDecimal.valueOf(usdRateToday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal eurAmount = BigDecimal.valueOf(50).multiply(eurRateToday.getValue())
                .divide(BigDecimal.valueOf(eurRateToday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal expected = rubAmount.add(usdAmount).add(eurAmount);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен вернуть ноль при пустом списке валют")
    void shouldReturnZeroForEmptyList() {
        // Given
        List<CurrencyAmountDto> amounts = List.of();

        // When
        BigDecimal result = exchangeRateService.convertListToRub(amounts);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Должен вернуть ноль при null списке валют")
    void shouldReturnZeroForNullList() {
        // When
        BigDecimal result = exchangeRateService.convertListToRub(null);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Должен использовать кэш при повторных вызовах")
    void shouldUseCacheForRepeatedCalls() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);

        // When - первый вызов
        BigDecimal result1 = exchangeRateService.convertToRub(amount, "USD");

        // Второй вызов с теми же параметрами
        BigDecimal result2 = exchangeRateService.convertToRub(amount, "USD");

        // Then
        assertThat(result1).isEqualByComparingTo(result2);
        // Кэш должен работать, но мы не можем проверить это напрямую в интеграционном тесте
        // Результаты должны быть одинаковыми
    }

    @Test
    @DisplayName("Должен найти ближайший курс по дате (меньше или равно)")
    void shouldFindNearestRateByDate() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        LocalDate futureDate = LocalDate.now().plusDays(2); // Курса на эту дату нет, должен взять сегодняшний

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "USD", futureDate);

        // Then
        BigDecimal expected = amount.multiply(usdRateToday.getValue())
                .divide(BigDecimal.valueOf(usdRateToday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен найти исторический курс по конкретной дате")
    void shouldFindHistoricalRateBySpecificDate() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "USD", yesterday);

        // Then
        BigDecimal expected = amount.multiply(usdRateYesterday.getValue())
                .divide(BigDecimal.valueOf(usdRateYesterday.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Должен вернуть true если курсы на дату не существуют")
    void shouldReturnTrueWhenRatesDoNotExistForDate() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(10);

        // When
        boolean result = exchangeRateService.needLoadForDate(futureDate);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Должен вернуть false если курсы на дату уже существуют")
    void shouldReturnFalseWhenRatesAlreadyExistForDate() {
        // Given
        LocalDate today = LocalDate.now();

        // When
        boolean result = exchangeRateService.needLoadForDate(today);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Должен корректно округлять результат конвертации")
    void shouldRoundConversionResultCorrectly() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(123.456);
        ExchangeRate preciseRate = new ExchangeRate();
        preciseRate.setCurrencyCode("USD");
        preciseRate.setCurrencyDate(LocalDate.now().plusDays(3));
        preciseRate.setCurrencyName("US Dollar");
        preciseRate.setNominal(1);
        preciseRate.setValue(BigDecimal.valueOf(91.2345));
        preciseRate.setVunitRate(BigDecimal.valueOf(91.2345));
        entityManager.persistAndFlush(preciseRate);

        // When
        BigDecimal result = exchangeRateService.convertToRub(amount, "USD", LocalDate.now().plusDays(3));

        // Then
        // Ожидаем округление до 2 знаков после запятой
        BigDecimal expected = amount.multiply(preciseRate.getValue())
                .divide(BigDecimal.valueOf(preciseRate.getNominal()), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(result).isEqualByComparingTo(expected);
        assertThat(result.scale()).isEqualTo(2);
    }
}