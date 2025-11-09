package ru.vvsem.bank.analyzer.services.analytics;

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
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.SeriesFilterDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CategoryMapperImpl;
import ru.vvsem.bank.analyzer.mappers.OperationTypeMapper;
import ru.vvsem.bank.analyzer.mappers.OperationTypeMapperImpl;
import ru.vvsem.bank.analyzer.mappers.TransactionMapperImpl;
import ru.vvsem.bank.analyzer.mappers.UserMapperImpl;
import ru.vvsem.bank.analyzer.models.*;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.repositories.UserRepository;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.services.card.CardService;
import ru.vvsem.bank.analyzer.services.category.CategoryService;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application-test.yml")
@Import({
        AnalyticsServiceImpl.class,
        CategoryMapperImpl.class,
        OperationTypeMapperImpl.class,
        TransactionMapperImpl.class,
        BankMapperImpl.class,
        UserMapperImpl.class,
        CardMapperImpl.class
})
class AnalyticsServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private EntityAccessProvider entityAccessProvider;

    private SecurityUser securityUser;
    private User user;
    private Category category1;
    private Category category2;
    private Currency currencyRub;
    private Currency currencyUsd;
    private Card card1;
    private Card card2;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        // Создаём пользователя
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = userRepository.saveAndFlush(user);

        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), null,
                true, true, true, true
        );

        // Мокаем сервисы
        when(customUserDetailsService.getUserById(user.getId())).thenReturn(user);

        // Создаём валюты
        currencyRub = new Currency("CU1", "₽", "Russian Ruble");
        currencyUsd = new Currency("CU2", "$", "US Dollar");
        entityManager.persistAndFlush(currencyRub);
        entityManager.persistAndFlush(currencyUsd);

        // Создаём категории
        category1 = new Category();
        category1.setName("Food");
        category1.setColor("#FF0000");
        category1.setUser(user);

        category2 = new Category();
        category2.setName("Salary");
        category2.setColor("#00FF00");
        category2.setUser(user);

        category1 = entityManager.persistAndFlush(category1);
        category2 = entityManager.persistAndFlush(category2);

        // Мокаем categoryService
        when(categoryService.getCategoryEntitiesForUser(securityUser)).thenReturn(
                List.of(category1, category2));

        // Создаём карты
        Bank bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        account = new ru.vvsem.bank.analyzer.models.BankAccount();
        account.setName("Acc1");
        account.setAccountNumber("40817810099910000001");
        account.setBalance(BigDecimal.valueOf(100));
        account.setUser(user);
        account.setCurrency(currencyRub);
        account.setBank(bank);
        account = entityManager.persistAndFlush(account);

        card1 = new Card();
        card1.setCardName("Test Card 1");
        card1.setIssuerBank(bank);
        card1.setLastFourDigits("1111");
        card1.setAccount(account);

        card2 = new Card();
        card2.setCardName("Test Card 2");
        card2.setIssuerBank(bank);
        card2.setLastFourDigits("2222");
        card2.setAccount(account);

        entityManager.persistAndFlush(card1);
        entityManager.persistAndFlush(card2);

        //
        when(customUserDetailsService.getUserById(user.getId())).thenReturn(user);
        when(entityAccessProvider.requireBank(bank.getId())).thenReturn(bank);
        when(entityAccessProvider.requireCurrency(currencyRub.getId())).thenReturn(currencyRub);
        when(entityAccessProvider.requireCurrency(currencyUsd.getId())).thenReturn(currencyUsd);
        when(entityAccessProvider.requireOwnedCard(card1.getId(),user.getId())).thenReturn(card1);
        when(entityAccessProvider.requireOwnedCard(card2.getId(),user.getId())).thenReturn(card2);


        // Мокаем exchange rate
        when(exchangeRateService.convertToRub(BigDecimal.valueOf(100), "CU1")).thenReturn(BigDecimal.valueOf(100));
        when(exchangeRateService.convertToRub(BigDecimal.valueOf(1000), "CU1")).thenReturn(BigDecimal.valueOf(1000));
        when(exchangeRateService.convertToRub(BigDecimal.valueOf(200), "CU2")).thenReturn(BigDecimal.valueOf(15000)); // 200 USD ≈ 15000 RUB
        when(exchangeRateService.convertToRub(BigDecimal.valueOf(50), "CU1")).thenReturn(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("Должен вернуть временной ряд по датам с заполнением пропущенных")
    void shouldReturnTimeSeries_WithFilledDates() {
        // Given
        Transaction t1 = new Transaction();
        t1.setDescription("Description 1");
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setCurrency(currencyRub);
        t1.setOperationTime(LocalDateTime.of(2024, 10, 1, 12, 0));
        t1.setCard(card1);
        t1.setCategory(category1);
        t1.setUser(user);
        t1.setOperationType(OperationType.OUTGOING);
        t1.setHide(false);

        Transaction t2 = new Transaction();
        t2.setDescription("Description 2");
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setCurrency(currencyUsd);
        t2.setOperationTime(LocalDateTime.of(2024, 10, 3, 14, 0));
        t2.setCard(card2);
        t2.setCategory(category2);
        t2.setUser(user);
        t2.setOperationType(OperationType.INCOMING);
        t2.setHide(false);

        entityManager.persistAndFlush(t1);
        entityManager.persistAndFlush(t2);

        SeriesFilterDto filter = new SeriesFilterDto();
        filter.setStartDate(LocalDate.of(2024, 10, 1));
        filter.setEndDate(LocalDate.of(2024, 10, 5));

        // When
        List<TimeSeriesDto> result = analyticsService.getTimeSeries(filter, securityUser);

        // Then
        assertThat(result).hasSize(5);
        assertThat(result.get(0)).hasFieldOrPropertyWithValue("date", LocalDate.of(2024, 10, 1));
        assertThat(result.get(0)).hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(100));

        assertThat(result.get(1)).hasFieldOrPropertyWithValue("date", LocalDate.of(2024, 10, 2));
        assertThat(result.get(1)).hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO);

        assertThat(result.get(2)).hasFieldOrPropertyWithValue("date", LocalDate.of(2024, 10, 3));
        assertThat(result.get(2)).hasFieldOrPropertyWithValue("amount", BigDecimal.valueOf(15000));

        assertThat(result.get(3)).hasFieldOrPropertyWithValue("date", LocalDate.of(2024, 10, 4));
        assertThat(result.get(3)).hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO);

        assertThat(result.get(4)).hasFieldOrPropertyWithValue("date", LocalDate.of(2024, 10, 5));
        assertThat(result.get(4)).hasFieldOrPropertyWithValue("amount", BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Должен вернуть распределение по категориям в рублях")
    void shouldReturnCategoryBreakdown_InRubles() {
        // Given
        Transaction t1 = new Transaction();
        t1.setDescription("Description 1");
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setCurrency(currencyRub);
        t1.setOperationTime(LocalDateTime.now());
        t1.setCard(card1);
        t1.setCategory(category1);
        t1.setUser(user);
        t1.setOperationType(OperationType.OUTGOING);
        t1.setHide(false);

        Transaction t2 = new Transaction();
        t2.setDescription("Description 2");
        t2.setAmount(BigDecimal.valueOf(50));
        t2.setCurrency(currencyRub);
        t2.setOperationTime(LocalDateTime.now());
        t2.setCard(card1);
        t2.setCategory(category1);
        t2.setUser(user);
        t2.setOperationType(OperationType.OUTGOING);
        t2.setHide(false);

        entityManager.persistAndFlush(t1);
        entityManager.persistAndFlush(t2);

        SeriesFilterDto filter = new SeriesFilterDto();
        filter.setStartDate(LocalDate.now().minusDays(1));
        filter.setEndDate(LocalDate.now().plusDays(1));

        // When
        List<CategoryBreakdownDto> result = analyticsService.getCategoryBreakdown(filter, securityUser);

        // Then
        assertThat(result).hasSize(1);
        CategoryBreakdownDto dto = result.get(0);
        assertThat(dto.getCategory().getName()).isEqualTo("Food");
        assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150)); // 100 + 50
    }

    @Test
    @DisplayName("Должен вернуть пустой временной ряд, если нет транзакций")
    void shouldReturnEmptyTimeSeries_WhenNoTransactions() {
        // Given
        SeriesFilterDto filter = new SeriesFilterDto();
        filter.setStartDate(LocalDate.of(2024, 1, 1));
        filter.setEndDate(LocalDate.of(2024, 1, 10));

        // When
        List<TimeSeriesDto> result = analyticsService.getTimeSeries(filter, securityUser);

        // Then
        assertThat(result).hasSize(10);
        assertThat(result)
                .allMatch(dto -> BigDecimal.ZERO.compareTo(dto.getAmount()) == 0);
    }

    @Test
    @DisplayName("Должен учитывать фильтр по карте")
    void shouldFilterByCardIdList() {
        // Given
        Transaction t1 = new Transaction();
        t1.setDescription("Description 1");
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setCurrency(currencyRub);
        t1.setOperationTime(LocalDateTime.of(2024, 10, 1, 12, 0));
        t1.setCard(card1);
        t1.setCategory(category1);
        t1.setUser(user);
        t1.setOperationType(OperationType.OUTGOING);
        t1.setHide(false);

        Transaction t2 = new Transaction();
        t2.setDescription("Description 2");
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setCurrency(currencyUsd);
        t2.setOperationTime(LocalDateTime.of(2024, 10, 1, 13, 0));
        t2.setCard(card2);
        t2.setCategory(category2);
        t2.setUser(user);
        t2.setOperationType(OperationType.INCOMING);
        t2.setHide(false);

        entityManager.persistAndFlush(t1);
        entityManager.persistAndFlush(t2);

        SeriesFilterDto filter = new SeriesFilterDto();
        filter.setStartDate(LocalDate.of(2024, 10, 1));
        filter.setEndDate(LocalDate.of(2024, 10, 1));
        filter.setCardIdList(List.of(card1.getId()));

        // When
        List<TimeSeriesDto> result = analyticsService.getTimeSeries(filter, securityUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Должен учитывать фильтр по типу операции")
    void shouldFilterByOperationType() {
        // Given
        Transaction incoming = new Transaction();
        incoming.setDescription("Incoming");
        incoming.setAmount(BigDecimal.valueOf(1000));
        incoming.setCurrency(currencyRub);
        incoming.setOperationTime(LocalDateTime.of(2024, 10, 1, 10, 0));
        incoming.setCard(card1);
        incoming.setCategory(category2);
        incoming.setUser(user);
        incoming.setOperationType(OperationType.INCOMING);
        incoming.setHide(false);

        Transaction outgoing = new Transaction();
        outgoing.setDescription("Outgoing");
        outgoing.setAmount(BigDecimal.valueOf(200));
        outgoing.setCurrency(currencyRub);
        outgoing.setOperationTime(LocalDateTime.of(2024, 10, 1, 11, 0));
        outgoing.setCard(card1);
        outgoing.setCategory(category1);
        outgoing.setUser(user);
        outgoing.setOperationType(OperationType.OUTGOING);
        outgoing.setHide(false);

        entityManager.persistAndFlush(incoming);
        entityManager.persistAndFlush(outgoing);

        SeriesFilterDto filter = new SeriesFilterDto();
        filter.setStartDate(LocalDate.of(2024, 10, 1));
        filter.setEndDate(LocalDate.of(2024, 10, 1));
        filter.setOperationTypeIdList(List.of(1)); // 1 = INCOMING

        // When
        List<TimeSeriesDto> result = analyticsService.getTimeSeries(filter, securityUser);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}