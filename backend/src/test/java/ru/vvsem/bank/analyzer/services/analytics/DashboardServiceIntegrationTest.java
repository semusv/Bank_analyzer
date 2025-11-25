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
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapperImpl;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionMapperImpl;
import ru.vvsem.bank.analyzer.models.*;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application.yml")
@Import({
        DashboardServiceImpl.class,
        TransactionMapperImpl.class,
        BankMapperImpl.class,
        BankAccountMapperImpl.class,
        CardMapperImpl.class
})
class DashboardServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private SecurityUser securityUser;
    private User user;
    private Currency currencyRub;
    private Currency currencyUsd;
    private Category category1;
    private Category category2;
    private Card card1;
    private BankAccount account1;
    private BankAccount account2;

    @BeforeEach
    void setUp() {
        // Создаём пользователя
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = entityManager.persistAndFlush(user);

        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), null,
                true, true, true, true
        );

        when(customUserDetailsService.getUserById(user.getId())).thenReturn(user);

        // Создаём валюты
        currencyRub = new Currency("RUB", "₽", "Russian Ruble");
        currencyUsd = new Currency("USD", "$", "US Dollar");
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

        // Создаём банк
        Bank bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        // Создаём счета
        account1 = new BankAccount();
        account1.setName("Acc1");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(BigDecimal.valueOf(1000));
        account1.setUser(user);
        account1.setCurrency(currencyRub);
        account1.setBank(bank);
        account1 = entityManager.persistAndFlush(account1);

        account2 = new BankAccount();
        account2.setName("Acc2");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(BigDecimal.valueOf(500));
        account2.setUser(user);
        account2.setCurrency(currencyUsd);
        account2.setBank(bank);
        account2 = entityManager.persistAndFlush(account2);

        // Создаём карту
        card1 = new Card();
        card1.setCardName("Test Card 1");
        card1.setIssuerBank(bank);
        card1.setLastFourDigits("1111");
        card1.setAccount(account1);
        entityManager.persistAndFlush(card1);

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("Должен вернуть корректную статистику для дашборда")
    void shouldReturnCorrectDashboardStats() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // Создаём транзакции за текущий месяц
        createTransaction("Salary", BigDecimal.valueOf(1000), currencyRub,
                now.minusDays(5), category2, OperationType.INCOMING);
        createTransaction("Bonus", BigDecimal.valueOf(200), currencyUsd,
                now.minusDays(3), category2, OperationType.INCOMING);
        createTransaction("Groceries", BigDecimal.valueOf(-300), currencyRub,
                now.minusDays(2), category1, OperationType.OUTGOING);
        createTransaction("Restaurant", BigDecimal.valueOf(-50), currencyUsd,
                now.minusDays(1), category1, OperationType.OUTGOING);
        createTransaction("Unknown", BigDecimal.valueOf(-100), currencyRub,
                now.minusDays(1), null, OperationType.OUTGOING);

        // Мокаем конвертацию в рубли
        when(exchangeRateService.convertListToRub(anyList()))
                .thenReturn(BigDecimal.valueOf(3000))  // 1-й вызов: monthlyIncome
                .thenReturn(BigDecimal.valueOf(-800))   // 2-й вызов: monthlyExpense
                .thenReturn(BigDecimal.valueOf(6000)); // 3-й вызов: totalBalance

        // When
        DashboardStatsDto result = dashboardService.getDashboardStats(securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMonthlyIncomeRub()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(result.getMonthlyExpenseRub()).isEqualByComparingTo(BigDecimal.valueOf(-800));
        assertThat(result.getTotalBalanceRub()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getTotalTransactions()).isEqualTo(5L);
        assertThat(result.getUncategorizedTransactions()).isEqualTo(1L);

        // Проверяем, что списки валют заполнены
        assertThat(result.getMonthlyIncomes()).hasSize(2);
        assertThat(result.getMonthlyExpenses()).hasSize(2);
        assertThat(result.getTotalBalances()).hasSize(2);
    }

    @Test
    @DisplayName("Должен вернуть нулевую статистику при отсутствии транзакций")
    void shouldReturnZeroStatsWhenNoTransactions() {
        // Given
        when(exchangeRateService.convertListToRub(anyList())).thenAnswer(invocation -> {
            List<CurrencyAmountDto> list = invocation.getArgument(0);
            if (list.isEmpty()) {
                return BigDecimal.ZERO;
            }
            // Для totalBalance (счета account1 и account2)
            if (list.size() == 2) {
                return BigDecimal.valueOf(6000);
            }
            return BigDecimal.ZERO;
        });

        // When
        DashboardStatsDto result = dashboardService.getDashboardStats(securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getMonthlyIncomeRub()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMonthlyExpenseRub()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalBalanceRub()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getTotalTransactions()).isZero();
        assertThat(result.getUncategorizedTransactions()).isZero();
    }

    @Test
    @DisplayName("Должен вернуть последние транзакции в правильном порядке")
    void shouldReturnRecentTransactionsInCorrectOrder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        createTransaction("Old", BigDecimal.valueOf(100), currencyRub,
                now.minusDays(3), category1, OperationType.OUTGOING);
        Transaction middle = createTransaction("Middle", BigDecimal.valueOf(200), currencyRub,
                now.minusDays(2), category1, OperationType.OUTGOING);
        Transaction newest = createTransaction("New", BigDecimal.valueOf(300), currencyRub,
                now.minusDays(1), category1, OperationType.OUTGOING);

        // When
        List<TransactionDto> result = dashboardService.getRecentTransactions(securityUser, 2);

        // Then
        assertThat(result).hasSize(2);
        // Проверяем порядок (от новых к старым)
        assertThat(result.get(0).getDescription()).isEqualTo(newest.getDescription());
        assertThat(result.get(1).getDescription()).isEqualTo(middle.getDescription());
    }

    @Test
    @DisplayName("Должен обрабатывать нулевые значения из репозитория")
    void shouldHandleNullValuesFromRepository() {
        // Given
        // Удаляем все транзакции
        transactionRepository.deleteAll();

       when(exchangeRateService.convertListToRub(anyList())).thenAnswer(invocation -> {
            List<CurrencyAmountDto> list = invocation.getArgument(0);
            if (list.isEmpty()) {
                return BigDecimal.ZERO;
            }
            // Для totalBalance (счета account1 и account2)
            if (list.size() == 2) {
                return BigDecimal.valueOf(6000);
            }
            return BigDecimal.ZERO;
        });

        // When
        DashboardStatsDto result = dashboardService.getDashboardStats(securityUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalTransactions()).isEqualByComparingTo(0L);
        assertThat(result.getUncategorizedTransactions()).isEqualByComparingTo(0L);
        assertThat(result.getTotalBalanceRub()).isEqualByComparingTo(BigDecimal.valueOf(6000));
        assertThat(result.getTotalBalances()).hasSize(2);
        assertThat(result.getMonthlyIncomeRub()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMonthlyIncomes()).isEmpty();
        assertThat(result.getMonthlyExpenseRub()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMonthlyExpenses()).isEmpty();
    }

    @Test
    @DisplayName("Должен корректно работать с лимитом для последних транзакций")
    void shouldRespectLimitForRecentTransactions() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 10; i++) {
            createTransaction("Transaction " + i, BigDecimal.valueOf(i * 100), currencyRub,
                    now.minusDays(i), category1, OperationType.OUTGOING);
        }

        // When
        List<TransactionDto> result = dashboardService.getRecentTransactions(securityUser, 5);

        // Then
        assertThat(result).hasSize(5);
        // Проверяем, что вернулись самые последние транзакции
        assertThat(result.get(0).getDescription()).isEqualTo("Transaction 0");
        assertThat(result.get(1).getDescription()).isEqualTo("Transaction 1");
        assertThat(result.get(2).getDescription()).isEqualTo("Transaction 2");
        assertThat(result.get(3).getDescription()).isEqualTo("Transaction 3");
        assertThat(result.get(4).getDescription()).isEqualTo("Transaction 4");
    }

    private Transaction createTransaction(String description, BigDecimal amount, Currency currency,
                                          LocalDateTime operationTime, Category category, OperationType operationType) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setOperationTime(operationTime);
        transaction.setCard(card1);
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setOperationType(operationType);
        transaction.setHide(false);
        return entityManager.persistAndFlush(transaction);
    }
}