package ru.vvsem.bank.analyzer.repositories;

import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application-test.yml")
class TransactionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private Currency usd;
    private Currency eur;
    private Category category;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setLogin("testLogin");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setSurname("TestSurname");
        testUser.setName("TestName");
        entityManager.persistAndFlush(testUser);

        usd = new Currency("CU1", "$", "Currency 1");
        eur = new Currency("CU2", "€", "Currency 2");
        entityManager.persistAndFlush(usd);
        entityManager.persistAndFlush(eur);

        category = new Category();
        category.setName("Test Category");
        category.setUser(testUser);
        entityManager.persistAndFlush(category);
    }

    @Test
    void shouldFindByIdAndUserId_WhenTransactionExists() {
        // given
        Transaction transaction = new Transaction();
        transaction.setUser(testUser);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setCurrency(usd);
        transaction.setOperationTime(LocalDateTime.now());
        transaction.setOperationType(OperationType.INCOMING);
        transaction.setDescription("Test transaction");
        transaction.setCategory(category);

        entityManager.persistAndFlush(transaction);

        // When
        Optional<Transaction> result = transactionRepository
                .findByIdAndUserId(transaction.getId(), testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAmount()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldNotFindByIdAndUserId_WhenWrongUser() {
        // ... аналогично, но с другим userId
        Long otherUserId = 999L;
        Optional<Transaction> result = transactionRepository.findByIdAndUserId(1L, otherUserId);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldCalculateMonthlyIncome_Correctly() {
        // Given

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = now.withDayOfMonth(28).withHour(23).withMinute(59);

        Transaction income1 = new Transaction();
        income1.setUser(testUser);
        income1.setAmount(BigDecimal.valueOf(100));
        income1.setCurrency(usd);
        income1.setOperationTime(start.plusDays(1));
        income1.setOperationType(OperationType.INCOMING);
        income1.setDescription("Test transaction");
        income1.setCategory(category);

        Transaction income2 = new Transaction();
        income2.setUser(testUser);
        income2.setAmount(BigDecimal.valueOf(200));
        income2.setCurrency(usd);
        income2.setOperationTime(start.plusDays(5));
        income2.setOperationType(OperationType.INCOMING);
        income2.setDescription("Test transaction");
        income2.setCategory(category);

        Transaction expense = new Transaction();
        expense.setUser(testUser);
        expense.setAmount(BigDecimal.valueOf(-50));
        expense.setCurrency(eur);
        expense.setOperationTime(start.plusDays(3));
        expense.setOperationType(OperationType.OUTGOING);
        expense.setDescription("Test transaction");
        expense.setCategory(category);

        entityManager.persistAndFlush(income1);
        entityManager.persistAndFlush(income2);
        entityManager.persistAndFlush(expense);

        // When
        List<CurrencyAmountDto> income = transactionRepository.calculateMonthlyIncome(testUser.getId(), start, end);

        // Then
        assertThat(income).hasSize(1);
        CurrencyAmountDto dto = income.get(0);
        assertThat(dto.getCurrencyCode()).isEqualTo("CU1");
        assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(300));
    }
}
