package ru.vvsem.bank.analyzer.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application-test.yml")
class JpaTransactionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final List<User> userList  = new ArrayList<>();
    private Currency usd;
    private Currency eur;
    private Currency rub;
    private final List<Category> categoryList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setLogin("testLogin");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setSurname("TestSurname");
        user.setName("TestName");
        entityManager.persistAndFlush(user);
        userList.add(user);

        user = new User();
        user.setLogin("testLogin2");
        user.setEmail("test2@example.com");
        user.setPassword("password2");
        user.setSurname("TestSurname2");
        user.setName("TestName2");
        entityManager.persistAndFlush(user);
        userList.add(user);



        usd = new Currency("CU1", "$", "Currency 1");
        eur = new Currency("CU2", "€", "Currency 2");
        rub = new Currency("CU3", "₽", "Currency 3");
        entityManager.persistAndFlush(usd);
        entityManager.persistAndFlush(eur);
        entityManager.persistAndFlush(rub);

        Category category = new Category();
        category.setName("Test Category 1");
        category.setUser(user);
        entityManager.persistAndFlush(category);
        categoryList.add(category);

        category = new Category();
        category.setName("Test Category 2");
        category.setUser(user);
        entityManager.persistAndFlush(category);
        categoryList.add(category);
    }

    @DisplayName("Должен найти транзакцию по id и userId")
    @Test
    void shouldFindByIdAndUserId_WhenTransactionExists() {
        // given
        Transaction transaction = createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(1000), rub, LocalDateTime.now());

        entityManager.persistAndFlush(transaction);

        // When
        Optional<Transaction> result = transactionRepository
                .findByIdAndUserId(transaction.getId(), userList.get(0).getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional, если транзакция не найдена по id и userId")
    void shouldNotFindByIdAndUserId_WhenWrongUser() {
        // Given
        Transaction transaction = createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(1000), rub, LocalDateTime.now());
        Long otherUserId = 999L;

        // When
        Optional<Transaction> result = transactionRepository.findByIdAndUserId(transaction.getId(), otherUserId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен рассчитать доходы за месяц по валютам")
    void shouldCalculateMonthlyIncome_ReturnSumOfPositiveTransactionsGroupedByCurrency() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = now.withDayOfMonth(28).withHour(23).withMinute(59);
        // Доходы
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(100), rub, start.plusDays(1), OperationType.INCOMING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(200), rub, start.plusDays(5), OperationType.INCOMING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(300), usd, start.plusDays(1), OperationType.INCOMING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(400), usd, start.plusDays(5), OperationType.INCOMING);
        // Расходы
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(-22), rub, start.plusDays(5), OperationType.OUTGOING);

        // When
        List<CurrencyAmountDto> income = transactionRepository.calculateMonthlyIncome(userList.get(0).getId(), start, end);

        // Then
        assertThat(income).hasSize(2);
        assertThat(income)
                .filteredOn(amountDto ->
                        "CU1".equals(amountDto.getCurrencyCode()))
                .first()
                .satisfies(amountDto ->
                        assertThat(amountDto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(700)));
        assertThat(income)
                .filteredOn(amountDto ->
                        "CU3".equals(amountDto.getCurrencyCode()))
                .first()
                .satisfies(amountDto ->
                        assertThat(amountDto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300)));
    }

    @Test
    @DisplayName("Должен найти все транзакции по userId")
    void shouldFindByUserId_ReturnAllTransactionsForUser() {
        // Given
        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(100), rub, LocalDateTime.now().minusDays(1));
        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(200), usd, LocalDateTime.now());

        // When
        List<Transaction> result = transactionRepository.findByUserId(userList.get(0).getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("amount").containsExactlyInAnyOrder(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200)
        );
    }

    @Test
    @DisplayName("Должен подсчитать количество транзакций по userId")
    void shouldCountByUserId_ReturnCorrectCount() {
        // Given
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(100), rub, LocalDateTime.now().minusDays(1));
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(200), usd, LocalDateTime.now());
        createAndPersistTransaction(
                userList.get(1), BigDecimal.valueOf(200), eur, LocalDateTime.now());

        // When
        Long count = transactionRepository.countByUserId(userList.get(0).getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен подсчитать количество транзакций без категории по userId")
    void shouldCountUncategorizedByUserId_ReturnOnlyNullCategoryTransactions() {
        // Given
        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(100), rub, LocalDateTime.now()); // без категории
        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(150), rub, LocalDateTime.now()); // без категории
        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(200), usd, LocalDateTime.now(), categoryList.get(0)); // с категорией
        createAndPersistTransaction(userList.get(1), BigDecimal.valueOf(100), rub, LocalDateTime.now()); // без категории

        // When
        Long count = transactionRepository.countUncategorizedByUserId(userList.get(0).getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен рассчитать доходы за месяц по валюте")
    void shouldCalculateMonthlyExpense_ReturnSumOfNegativeTransactionsGroupedByCurrency() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = now.withDayOfMonth(28).withHour(23).withMinute(59);
        // Доходы
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(-100), rub, start.plusDays(1), OperationType.OUTGOING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(-200), rub, start.plusDays(5), OperationType.OUTGOING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(-300), usd, start.plusDays(1), OperationType.OUTGOING);
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(-400), usd, start.plusDays(5), OperationType.OUTGOING);
        // Расходы
        createAndPersistTransaction(
                userList.get(0), BigDecimal.valueOf(22), rub, start.plusDays(5), OperationType.INCOMING);

        // When
        List<CurrencyAmountDto> outgoing = transactionRepository.calculateMonthlyExpense(userList.get(0).getId(), start, end);

        // Then
        assertThat(outgoing).hasSize(2);
        assertThat(outgoing)
                .filteredOn(amountDto ->
                        "CU1".equals(amountDto.getCurrencyCode()))
                .first()
                .satisfies(amountDto ->
                        assertThat(amountDto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-700)));
        assertThat(outgoing)
                .filteredOn(amountDto ->
                        "CU3".equals(amountDto.getCurrencyCode()))
                .first()
                .satisfies(amountDto ->
                        assertThat(amountDto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(-300)));
    }

    @Test
    @DisplayName("Должен найти N последних транзакций по userId, отсортированных по дате операции")
    void shouldFindTopNByUserIdOrderByOperationTimeDesc_ReturnLatestTransactions() {
        // Given
        LocalDateTime time1 = LocalDateTime.now().minusDays(5);
        LocalDateTime time2 = LocalDateTime.now().minusDays(3);
        LocalDateTime time3 = LocalDateTime.now().minusDays(1);

        createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(100), rub, time1);
        Transaction t2 = createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(200), usd, time2);
        Transaction t3 = createAndPersistTransaction(userList.get(0), BigDecimal.valueOf(300), rub, time3);

        // When
        List<Transaction> result = transactionRepository.findTopNByUserIdOrderByOperationTimeDesc(userList.get(0).getId(), 2);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(t3.getId()); // самый свежий
        assertThat(result.get(1).getId()).isEqualTo(t2.getId()); // второй по свежести
    }



    private Transaction createAndPersistTransaction(
            User user,
            BigDecimal amount,
            Currency currency,
            LocalDateTime operationTime) {
        return createAndPersistTransaction(
                user,
                amount,
                currency,
                operationTime,
                null,
                OperationType.INCOMING);
    }

    private Transaction createAndPersistTransaction(
            User user,
            BigDecimal amount,
            Currency currency,
            LocalDateTime operationTime,
            Category category) {
        return createAndPersistTransaction(
                user,
                amount,
                currency,
                operationTime,
                category,
                OperationType.INCOMING);
    }



    private Transaction createAndPersistTransaction(
            User user,
            BigDecimal amount,
            Currency currency,
            LocalDateTime operationTime,
            OperationType operationType) {
        return createAndPersistTransaction(
                user,
                amount,
                currency,
                operationTime,
                null,
                operationType);
    }

    private Transaction createAndPersistTransaction(
            User user,
            BigDecimal amount,
            Currency currency,
            LocalDateTime operationTime,
            Category category,
            OperationType operationType
    ) {
        Transaction t = new Transaction();
        t.setDescription("Test Transaction");
        t.setUser(user);
        t.setAmount(amount);
        t.setCurrency(currency);
        t.setOperationTime(operationTime);
        t.setCategory(category);
        t.setOperationType(operationType);
        return entityManager.persistAndFlush(t);
    }
}
