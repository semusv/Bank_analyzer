package ru.vvsem.bank.analyzer.services.transaction;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionFilterDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapperImpl;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.mappers.TransactionMapperImpl;
import ru.vvsem.bank.analyzer.models.*;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.analytics.DashboardServiceImpl;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource("classpath:application.yml")
@Import({
        TransactionSearchServiceImpl.class,
        TransactionServiceImpl.class,
        DashboardServiceImpl.class,
        BankMapperImpl.class,
        TransactionMapperImpl.class,
        BankAccountMapperImpl.class,
        CardMapperImpl.class,
        EntityAccessProviderImpl.class,
})
class TransactionSearchServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private TransactionSearchService transactionSearchService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private ExchangeRateService mockExchangeRateService;

    private SecurityUser securityUser;
    private User user;
    private User anotherUser;
    private Currency currencyRub;
    private Category categoryFood;
    private Category categorySalary;
    private Card card1;
    private Card card2;
    private Bank bank1;
    private Bank bank2;
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;
    private Transaction transaction4;


    private Logger sqlLogger;
    private Logger binderLogger;
    private Level originalSqlLevel;
    private Level originalBinderLevel;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        transactionRepository.deleteAll();

        // Создаём пользователей
        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user = entityManager.persistAndFlush(user);

        anotherUser = new User();
        anotherUser.setLogin("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("pass");
        anotherUser.setName("Another");
        anotherUser.setSurname("User");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), null,
                true, true, true, true
        );

        // Создаём валюту
        currencyRub = new Currency("RUB", "₽", "Russian Ruble");
        entityManager.persistAndFlush(currencyRub);

        // Создаём категории
        categoryFood = new Category();
        categoryFood.setName("Food");
        categoryFood.setColor("#FF0000");
        categoryFood.setUser(user);
        categoryFood = entityManager.persistAndFlush(categoryFood);

        categorySalary = new Category();
        categorySalary.setName("Salary");
        categorySalary.setColor("#00FF00");
        categorySalary.setUser(user);
        categorySalary = entityManager.persistAndFlush(categorySalary);

        // Создаём банки
        bank1 = new Bank();
        bank1.setName("Sberbank");
        bank1.setBankCode("sber");
        bank1.setBic("11111111");
        bank1 = entityManager.persistAndFlush(bank1);

        bank2 = new Bank();
        bank2.setName("Tinkoff");
        bank2.setBankCode("tinkoff");
        bank2.setBic("22222222");
        bank2 = entityManager.persistAndFlush(bank2);

        // Создаём счета
        BankAccount account1 = new BankAccount();
        account1.setName("Sber Account");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setUser(user);
        account1.setCurrency(currencyRub);
        account1.setBank(bank1);
        account1 = entityManager.persistAndFlush(account1);

        BankAccount account2 = new BankAccount();
        account2.setName("Tinkoff Account");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(new BigDecimal("500.00"));
        account2.setUser(user);
        account2.setCurrency(currencyRub);
        account2.setBank(bank2);
        account2 = entityManager.persistAndFlush(account2);

        // Создаём карты
        card1 = new Card();
        card1.setCardName("Sber Card");
        card1.setLastFourDigits("1111");
        card1.setAccount(account1);
        card1.setIssuerBank(bank1);
        card1 = entityManager.persistAndFlush(card1);

        card2 = new Card();
        card2.setCardName("Tinkoff Card");
        card2.setLastFourDigits("2222");
        card2.setAccount(account2);
        card2.setIssuerBank(bank2);
        card2 = entityManager.persistAndFlush(card2);

        LocalDateTime now = LocalDateTime.now();

        // Создаём тестовые транзакции
        transaction1 = createTransaction(
                "Grocery shopping at supermarket",
                new BigDecimal("-1500.00"),
                now.minusDays(5),
                categoryFood,
                card1,
                user,
                OperationType.OUTGOING
        );

        transaction2 = createTransaction(
                "Monthly salary",
                new BigDecimal("50000.00"),
                now.minusDays(3),
                categorySalary,
                card1,
                user,
                OperationType.INCOMING
        );

        transaction3 = createTransaction(
                "Restaurant dinner",
                new BigDecimal("-2500.00"),
                now.minusDays(2),
                categoryFood,
                card2,
                user,
                OperationType.OUTGOING
        );

        transaction4 = createTransaction(
                "Online shopping",
                new BigDecimal("-3000.00"),
                now.minusDays(1),
                null,
                card2,
                user,
                OperationType.OUTGOING
        );

        // Транзакция другого пользователя
        createTransaction(
                "Other user transaction",
                new BigDecimal("-100.00"),
                now,
                null,
                card1,
                anotherUser,
                OperationType.OUTGOING
        );

        System.out.println("-------------------------------------------");
        System.out.println("-------------------------------------------");
        System.out.println("-------------------------------------------");
    }

    @Test
    @DisplayName("Должен вернуть все транзакции пользователя при пустом фильтре")
    void shouldReturnAllUserTransactionsWithEmptyFilter() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder().build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по категории")
    void shouldFilterTransactionsByCategory() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .categoryId(categoryFood.getId())
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TransactionDtoWithSiblings::getDescription)
                .containsExactlyInAnyOrder("Grocery shopping at supermarket", "Restaurant dinner");
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по карте")
    void shouldFilterTransactionsByCard() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .cardId(card1.getId())
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TransactionDtoWithSiblings::getDescription)
                .containsExactlyInAnyOrder("Grocery shopping at supermarket", "Monthly salary");
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по банку")
    void shouldFilterTransactionsByBank() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .bankId(bank2.getId())
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TransactionDtoWithSiblings::getDescription)
                .containsExactlyInAnyOrder("Restaurant dinner", "Online shopping");
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по описанию")
    void shouldFilterTransactionsByDescription() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .description("shopping")
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TransactionDtoWithSiblings::getDescription)
                .containsExactlyInAnyOrder("Grocery shopping at supermarket", "Online shopping");
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по диапазону дат")
    void shouldFilterTransactionsByDateRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(4);
        LocalDate endDate = LocalDate.now().minusDays(2);

        TransactionFilterDto filter = TransactionFilterDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(TransactionDtoWithSiblings::getDescription)
                .containsExactlyInAnyOrder("Monthly salary", "Restaurant dinner");
    }

    @Test
    @DisplayName("Должен фильтровать транзакции по нескольким критериям")
    void shouldFilterTransactionsByMultipleCriteria() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .categoryId(categoryFood.getId())
                .cardId(card2.getId())
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Restaurant dinner");
    }

    @Test
    @DisplayName("Должен поддерживать пагинацию")
    void shouldSupportPagination() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder().build();

        // When - первая страница с 2 элементами
        Page<TransactionDtoWithSiblings> page1 = transactionSearchService.searchTransactions(securityUser, filter, 0, 2);

        // Then
        assertThat(page1.getContent()).hasSize(2);
        assertThat(page1.getTotalElements()).isEqualTo(4);
        assertThat(page1.getTotalPages()).isEqualTo(2);
        assertThat(page1.getNumber()).isEqualTo(0);
        assertThat(page1.getSize()).isEqualTo(2);

        // When - вторая страница с 2 элементами
        Page<TransactionDtoWithSiblings> page2 = transactionSearchService.searchTransactions(securityUser, filter, 1, 2);

        // Then
        assertThat(page2.getContent()).hasSize(2);
        assertThat(page2.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("Должен возвращать пустой результат когда нет совпадений")
    void shouldReturnEmptyResultWhenNoMatches() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .description("nonexistent")
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Должен игнорировать пустое описание в фильтре")
    void shouldIgnoreEmptyDescription() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .description("")
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("Должен игнорировать null значения в фильтре")
    void shouldIgnoreNullValuesInFilter() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .categoryId(null)
                .cardId(null)
                .bankId(null)
                .description(null)
                .startDate(null)
                .endDate(null)
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("Должен возвращать транзакции в порядке убывания даты операции")
    void shouldReturnTransactionsInDescendingOrder() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder().build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        List<TransactionDtoWithSiblings> content = result.getContent();
        assertThat(content).isSortedAccordingTo((t1, t2) ->
                t2.getOperationTime().compareTo(t1.getOperationTime())
        );
    }

    @Test
    @DisplayName("Должен корректно обрабатывать регистр при поиске по описанию")
    void shouldHandleCaseInsensitiveSearch() {
        // Given
        TransactionFilterDto filter = TransactionFilterDto.builder()
                .description("GROCERY")
                .build();

        // When
        Page<TransactionDtoWithSiblings> result = transactionSearchService.searchTransactions(securityUser, filter, 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Grocery shopping at supermarket");
    }

    private Transaction createTransaction(String description, BigDecimal amount, LocalDateTime operationTime,
                                          Category category, Card card, User user, OperationType operationType) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setCurrency(currencyRub);
        transaction.setOperationTime(operationTime);
        transaction.setCard(card);
        transaction.setCategory(category);
        transaction.setUser(user);
        transaction.setOperationType(operationType);
        transaction.setHide(false);
        return entityManager.persistAndFlush(transaction);
    }
}