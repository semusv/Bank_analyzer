package ru.vvsem.bank.analyzer.services.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapperImpl;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.mappers.transaction.NewTransactionMapperImpl;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionHierarchyMapperImpl;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionMapperImpl;
import ru.vvsem.bank.analyzer.models.*;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.analytics.DashboardServiceImpl;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DataJpaTest
@TestPropertySource("classpath:application.yml")
@Import({
        TransactionServiceImpl.class,
        DashboardServiceImpl.class,
        BankMapperImpl.class,
        TransactionMapperImpl.class,
        TransactionHierarchyMapperImpl.class,
        NewTransactionMapperImpl.class,
        BankAccountMapperImpl.class,
        CardMapperImpl.class,
        EntityAccessProviderImpl.class,
})
class TransactionServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityAccessProvider entityAccessProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private ExchangeRateService mockExchangeRateService;

    private SecurityUser securityUser;
    private User user;
    private User anotherUser;
    private Currency currencyRub;
    private Category category;
    private Card card;
    private Transaction transaction1;
    private Transaction transaction2;

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

        // Создаём категорию
        category = new Category();
        category.setName("Food");
        category.setColor("#FF0000");
        category.setUser(user);
        category = entityManager.persistAndFlush(category);

        // Создаём банк
        Bank bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        // Создаём счёт
        BankAccount account = new BankAccount();
        account.setName("Main Account");
        account.setAccountNumber("40817810099910000001");
        account.setBalance(new BigDecimal("1000.00"));
        account.setUser(user);
        account.setCurrency(currencyRub);
        account.setBank(bank);
        account = entityManager.persistAndFlush(account);

        // Создаём карту
        card = new Card();
        card.setCardName("Test Card");
        card.setLastFourDigits("1234");
        card.setAccount(account);
        card.setIssuerBank(bank);
        card = entityManager.persistAndFlush(card);

        // Создаём тестовые транзакции
        transaction1 = new Transaction();
        transaction1.setDescription("Grocery shopping");
        transaction1.setAmount(new BigDecimal("-500.00"));
        transaction1.setCurrency(currencyRub);
        transaction1.setOperationTime(LocalDateTime.now().minusDays(2));
        transaction1.setCard(card);
        transaction1.setCategory(category);
        transaction1.setUser(user);
        transaction1.setOperationType(OperationType.OUTGOING);
        transaction1.setHide(false);
        transaction1 = entityManager.persistAndFlush(transaction1);

        transaction2 = new Transaction();
        transaction2.setDescription("Salary");
        transaction2.setAmount(new BigDecimal("2000.00"));
        transaction2.setCurrency(currencyRub);
        transaction2.setOperationTime(LocalDateTime.now().minusDays(1));
        transaction2.setCard(card);
        transaction2.setCategory(null);
        transaction2.setUser(user);
        transaction2.setOperationType(OperationType.INCOMING);
        transaction2.setHide(false);
        transaction2 = entityManager.persistAndFlush(transaction2);

        // Транзакция другого пользователя
        Transaction anotherUserTransaction = new Transaction();
        anotherUserTransaction.setDescription("Other user transaction");
        anotherUserTransaction.setAmount(new BigDecimal("-100.00"));
        anotherUserTransaction.setCurrency(currencyRub);
        anotherUserTransaction.setOperationTime(LocalDateTime.now());
        anotherUserTransaction.setCard(card);
        anotherUserTransaction.setCategory(null);
        anotherUserTransaction.setUser(anotherUser);
        anotherUserTransaction.setOperationType(OperationType.OUTGOING);
        anotherUserTransaction.setHide(false);
        entityManager.persistAndFlush(anotherUserTransaction);

        System.out.println("-------------------------------------------");
        System.out.println("--------------End of setup-----------------");
        System.out.println("-------------------------------------------");
    }

    @Test
    @DisplayName("Должен вернуть все транзакции пользователя")
    void shouldReturnAllUserTransactions() {
        // When
        List<TransactionDto> result = transactionService.getUserTransactionDtoList(user.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(TransactionDto::getDescription)
                .containsExactlyInAnyOrder("Grocery shopping", "Salary");
        for (TransactionDto transactionDto : result) {
            if (Objects.equals(transactionDto.getId(), transaction1.getId())) {
                assertAllFieldsInitialized(transactionDto, "subTransactions", "parentTransactionId");
            }
            if (Objects.equals(transactionDto.getId(), transaction2.getId())) {
                assertAllFieldsInitialized(transactionDto, "subTransactions", "parentTransactionId", "categoryId");
            }

        }

    }

    @Test
    @DisplayName("Должен вернуть пустой список когда у пользователя нет транзакций")
    void shouldReturnEmptyListWhenUserHasNoTransactions() {
        // Given
        User newUser = new User();
        newUser.setLogin("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("pass");
        newUser.setName("New");
        newUser.setSurname("User");
        newUser = entityManager.persistAndFlush(newUser);

        // When
        List<TransactionDto> result = transactionService.getUserTransactionDtoList(newUser.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть конкретную транзакцию пользователя")
    void shouldReturnSpecificUserTransaction() {
        // When
        TransactionDto result = transactionService.getTransactionDtoByIdAndUserId(transaction1.getId(), user.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transaction1.getId());
        assertThat(result.getDescription()).isEqualTo("Grocery shopping");
        assertThat(result.getAmount()).isEqualByComparingTo("-500.00");
    }

    @Test
    @DisplayName("Должен выбросить исключение при попытке получить чужую транзакцию")
    void shouldThrowExceptionWhenGettingOtherUserTransaction() {
        Long anotherUserId = anotherUser.getId();
        Long transactionId = transaction1.getId();
        // When & Then
        assertThrows(
                AccessDeniedException.class,
                () -> transactionService.getTransactionDtoByIdAndUserId(transactionId, anotherUserId)
        );
    }

    @Test
    @DisplayName("Должен удалить транзакцию пользователя")
    void shouldDeleteUserTransaction() {
        // Given
        Long transactionId = transaction1.getId();
        assertThat(entityManager.find(Transaction.class, transactionId)).isNotNull();

        // When
        transactionService.deleteTransaction(transactionId, securityUser);

        // Then
        assertThat(entityManager.find(Transaction.class, transactionId)).isNull();
    }

    @Test
    @DisplayName("Должен переключить флаг hide у транзакции")
    void shouldToggleHideFlag() {
        // Given
        boolean initialHideState = transaction1.isHide();
        assertThat(initialHideState).isFalse();

        // When
        TransactionDto result = transactionService.hideTransaction(transaction1.getId(), user.getId());

        // Then
        //noinspection ConstantValue
        assertThat(result.isHide()).isEqualTo(!initialHideState);

        // Проверяем, что состояние сохранилось в БД
        Transaction updatedTransaction = entityManager.find(Transaction.class, transaction1.getId());
        //noinspection ConstantValue
        assertThat(updatedTransaction.isHide()).isEqualTo(!initialHideState);
    }

    @Test
    @DisplayName("Должен обновить транзакцию")
    void shouldUpdateTransaction() {
        // Given
        transaction1.setDescription("Updated description");
        transaction1.setAmount(new BigDecimal("-600.00"));

        // When
        TransactionDto result = transactionService.updateTransaction(transaction1, user.getId());

        // Then
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getAmount()).isEqualByComparingTo("-600.00");

        // Проверяем, что изменения сохранились в БД
        Transaction updatedTransaction = entityManager.find(Transaction.class, transaction1.getId());
        assertThat(updatedTransaction.getDescription()).isEqualTo("Updated description");
        assertThat(updatedTransaction.getAmount()).isEqualByComparingTo("-600.00");
    }

    @Test
    @DisplayName("Должен создать новую транзакцию")
    void shouldCreateTransaction() {
        // Given
        Transaction newTransaction = new Transaction();
        newTransaction.setDescription("New transaction");
        newTransaction.setAmount(new BigDecimal("-300.00"));
        newTransaction.setCurrency(currencyRub);
        newTransaction.setOperationTime(LocalDateTime.now());
        newTransaction.setCard(card);
        newTransaction.setCategory(category);
        newTransaction.setUser(user);
        newTransaction.setOperationType(OperationType.OUTGOING);
        newTransaction.setHide(false);

        // When
        TransactionDto result = transactionService.createTransaction(newTransaction);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("New transaction");
        assertThat(result.getAmount()).isEqualByComparingTo("-300.00");

        // Проверяем, что транзакция сохранена в БД
        Transaction savedTransaction = entityManager.find(Transaction.class, result.getId());
        assertThat(savedTransaction).isNotNull();
        assertThat(savedTransaction.getDescription()).isEqualTo("New transaction");
    }

    @Test
    @DisplayName("Должен выполнить поиск транзакций с пагинацией")
    void shouldSearchTransactionsWithPagination() {
        // Given
        Specification<Transaction> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), user.getId());

        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<TransactionDtoWithSiblings> result = transactionService.searchTransactions(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Должен выполнить поиск по описанию транзакции")
    void shouldSearchTransactionsByDescription() {
        // Given
        Specification<Transaction> spec = (root, query, cb) ->
                cb.and(
                        cb.equal(root.get("user").get("id"), user.getId()),
                        cb.like(root.get("description"), "%Grocery%")
                );

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TransactionDtoWithSiblings> result = transactionService.searchTransactions(spec, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Grocery shopping");
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении чужой транзакции")
    void shouldThrowExceptionWhenUpdatingOtherUserTransaction() {
        Long anotherUserId = anotherUser.getId();
        // When & Then
        assertThrows(
                AccessDeniedException.class,
                () -> transactionService.updateTransaction(transaction1, anotherUserId)
        );
    }

    @Test
    @DisplayName("Должен корректно обрабатывать транзакцию без категории")
    void shouldHandleTransactionWithoutCategory() {
        // When
        TransactionDto result = transactionService.getTransactionDtoByIdAndUserId(transaction2.getId(), user.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryId()).isNull();
        assertThat(result.getDescription()).isEqualTo("Salary");
    }
}