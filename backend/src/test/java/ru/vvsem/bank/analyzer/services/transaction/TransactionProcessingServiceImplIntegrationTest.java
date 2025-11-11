package ru.vvsem.bank.analyzer.services.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.PatchTransactionData;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapperImpl;
import ru.vvsem.bank.analyzer.mappers.BankMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapperImpl;
import ru.vvsem.bank.analyzer.mappers.TransactionMapperImpl;
import ru.vvsem.bank.analyzer.mappers.UserMapperImpl;
import ru.vvsem.bank.analyzer.models.*;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.bank.BankServiceImpl;
import ru.vvsem.bank.analyzer.services.bank_account.BankAccountServiceImpl;
import ru.vvsem.bank.analyzer.services.card.CardServiceImpl;
import ru.vvsem.bank.analyzer.services.currency.CurrencyService;
import ru.vvsem.bank.analyzer.services.currency.CurrencyServiceImpl;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({
        TransactionProcessingServiceImpl.class,
        TransactionServiceImpl.class,
        BankAccountServiceImpl.class,
        CardServiceImpl.class,
        EntityAccessProviderImpl.class,
        TransactionMapperImpl.class,
        TransactionSearchServiceImpl.class,
        BankMapperImpl.class,
        BankAccountMapperImpl.class,
        CardMapperImpl.class,
        UserMapperImpl.class,
        CurrencyServiceImpl.class,
        BankServiceImpl.class,
        CurrencyMapperImpl.class
})
class TransactionProcessingServiceImplIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private TransactionProcessingService transactionProcessingService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @MockitoBean
    private CustomUserDetailsService userService;

    @MockitoBean
    private ExchangeRateService mockExchangeRateService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CurrencyService currencyService;

    private SecurityUser securityUser;
    private User user;
    private Currency currencyRub;
    private Category categoryFood;
    private Category categorySalary;
    private Card card1;
    private Card card2;
    private BankAccount account1;
    private BankAccount account2;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом
        transactionRepository.deleteAll();

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

        // Мокаем сервис пользователей
        when(userService.getUserById(user.getId())).thenReturn(user);

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

        // Создаём банк
        Bank bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        // Создаём счета
        account1 = new BankAccount();
        account1.setName("Main Account");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(new BigDecimal("10000.00"));
        account1.setUser(user);
        account1.setCurrency(currencyRub);
        account1.setBank(bank);
        account1 = entityManager.persistAndFlush(account1);

        account2 = new BankAccount();
        account2.setName("Secondary Account");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(new BigDecimal("5000.00"));
        account2.setUser(user);
        account2.setCurrency(currencyRub);
        account2.setBank(bank);
        account2 = entityManager.persistAndFlush(account2);

        // Создаём карты
        card1 = new Card();
        card1.setCardName("Main Card");
        card1.setLastFourDigits("1111");
        card1.setAccount(account1);
        card1.setIssuerBank(bank);
        card1 = entityManager.persistAndFlush(card1);

        card2 = new Card();
        card2.setCardName("Secondary Card");
        card2.setLastFourDigits("2222");
        card2.setAccount(account2);
        card2.setIssuerBank(bank);
        card2 = entityManager.persistAndFlush(card2);

        // Создаём существующую транзакцию для тестов
        existingTransaction = new Transaction();
        existingTransaction.setDescription("Original transaction");
        existingTransaction.setAmount(new BigDecimal("-1000.00"));
        existingTransaction.setCurrency(currencyRub);
        existingTransaction.setOperationTime(LocalDateTime.now().minusDays(1));
        existingTransaction.setCard(card1);
        existingTransaction.setCategory(categoryFood);
        existingTransaction.setUser(user);
        existingTransaction.setOperationType(OperationType.OUTGOING);
        existingTransaction.setHide(false);
        existingTransaction.setMaster(false);
        existingTransaction = entityManager.persistAndFlush(existingTransaction);

        System.out.println("---------------------------------------");
        System.out.println("Setup complete");
        System.out.println("---------------------------------------");
    }

    @Test
    @DisplayName("Должен создать исходящую транзакцию и обновить баланс счета")
    void shouldCreateOutgoingTransactionAndUpdateBalance() {
        // Given
        NewTransactionDto dto = NewTransactionDto.builder()
                .description("Grocery shopping")
                .amount(new BigDecimal("1500.00"))
                .operationTime(LocalDateTime.now())
                .categoryId(categoryFood.getId())
                .cardId(card1.getId())
                .operationType(OperationType.OUTGOING)
                .revCardId(null)
                .build();

        BigDecimal initialBalance = account1.getBalance();

        // When
        List<TransactionDto> result = transactionProcessingService.createTransaction(dto, securityUser);

        // Then
        entityManager.flush();
        entityManager.clear();
        assertThat(result).hasSize(1);
        TransactionDto transactionDto = result.get(0);
        assertThat(transactionDto.getDescription()).isEqualTo("Grocery shopping");
        assertThat(transactionDto.getAmount()).isEqualByComparingTo("-1500.00");
        assertThat(transactionDto.getOperationType()).isEqualTo(OperationType.OUTGOING);

        // Проверяем обновление баланса
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance.subtract(new BigDecimal("1500.00")));
    }

    @Test
    @DisplayName("Должен создать входящую транзакцию и обновить баланс счета")
    void shouldCreateIncomingTransactionAndUpdateBalance() {
        // Given
        BigDecimal amount = new BigDecimal("50000.00");
        NewTransactionDto dto = NewTransactionDto.builder()
                .description("Salary")
                .amount(amount)
                .operationTime(LocalDateTime.now())
                .categoryId(categorySalary.getId())
                .cardId(card1.getId())
                .operationType(OperationType.INCOMING)
                .revCardId(null)
                .build();

        BigDecimal initialBalance = account1.getBalance();

        // When
        when(userService.getUserById(securityUser.getId())).thenReturn(user);
        List<TransactionDto> result = transactionProcessingService.createTransaction(dto, securityUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(result).hasSize(1);
        TransactionDto transactionDto = result.get(0);
        assertThat(transactionDto.getDescription()).isEqualTo("Salary");
        assertThat(transactionDto.getAmount()).isEqualByComparingTo("50000.00");
        assertThat(transactionDto.getOperationType()).isEqualTo(OperationType.INCOMING);

        // Проверяем обновление баланса
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        BigDecimal expectedBalance = initialBalance.add(amount);
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("Должен создать перевод между картами и обновить балансы обоих счетов")
    void shouldCreateTransferBetweenCardsAndUpdateBothBalances() {
        // Given
        NewTransactionDto dto = NewTransactionDto.builder()
                .description("Transfer to secondary card")
                .amount(new BigDecimal("2000.00"))
                .operationTime(LocalDateTime.now())
                .categoryId(categoryFood.getId())
                .cardId(card1.getId())
                .operationType(OperationType.OUTGOING)
                .revCardId(card2.getId())
                .build();

        BigDecimal initialBalance1 = account1.getBalance();
        BigDecimal initialBalance2 = account2.getBalance();

        // When
        List<TransactionDto> result = transactionProcessingService.createTransaction(dto, securityUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(result).hasSize(2);

        // Проверяем первую транзакцию (исходящая)
        TransactionDto outgoingTransaction = result.get(1); // Основная транзакция
        assertThat(outgoingTransaction.getAmount()).isEqualByComparingTo("-2000.00");

        // Проверяем вторую транзакцию (входящая на другую карту)
        TransactionDto incomingTransaction = result.get(0); // Обратная транзакция
        assertThat(incomingTransaction.getAmount()).isEqualByComparingTo("2000.00");

        // Проверяем обновление балансов
        BankAccount updatedAccount1 = entityManager.find(BankAccount.class, account1.getId());
        BankAccount updatedAccount2 = entityManager.find(BankAccount.class, account2.getId());

        assertThat(updatedAccount1.getBalance()).isEqualByComparingTo(initialBalance1.subtract(new BigDecimal("2000.00")));
        assertThat(updatedAccount2.getBalance()).isEqualByComparingTo(initialBalance2.add(new BigDecimal("2000.00")));
    }

    @Test
    @DisplayName("Должен выбросить исключение при переводе между картами с разными валютами")
    void shouldThrowExceptionWhenTransferBetweenDifferentCurrencies() {
        // Given
        // Создаём другую валюту и счёт с этой валютой
        Currency currencyUsd = new Currency("USD", "$", "US Dollar");
        entityManager.persistAndFlush(currencyUsd);

        BankAccount usdAccount = new BankAccount();
        usdAccount.setName("USD Account");
        usdAccount.setAccountNumber("40817810099910000003");
        usdAccount.setBalance(new BigDecimal("1000.00"));
        usdAccount.setUser(user);
        usdAccount.setCurrency(currencyUsd);
        usdAccount.setBank(account1.getBank());
        usdAccount = entityManager.persistAndFlush(usdAccount);

        Card usdCard = new Card();
        usdCard.setCardName("USD Card");
        usdCard.setLastFourDigits("3333");
        usdCard.setAccount(usdAccount);
        usdCard.setIssuerBank(account1.getBank());
        usdCard = entityManager.persistAndFlush(usdCard);

        NewTransactionDto dto = NewTransactionDto.builder()
                .description("Transfer to USD card")
                .amount(new BigDecimal("2000.00"))
                .operationTime(LocalDateTime.now())
                .categoryId(categoryFood.getId())
                .cardId(card1.getId())
                .operationType(OperationType.OUTGOING)
                .revCardId(usdCard.getId())
                .build();

        // When & Then
        when(userService.getUserById(securityUser.getId())).thenReturn(user);
        assertThrows(IllegalArgumentException.class,
                () -> transactionProcessingService.createTransaction(dto, securityUser));
    }

    @Test
    @DisplayName("Должен разделить транзакцию на несколько подтранзакций")
    void shouldSplitTransactionIntoSubTransactions() {
        // Given
        List<SubTransactionDto> subTransactions = List.of(
                new SubTransactionDto("Food", new BigDecimal("-400.00")),
                new SubTransactionDto("Transport", new BigDecimal("-300.00")),
                new SubTransactionDto("Entertainment", new BigDecimal("-300.00"))
        );

        // When
        transactionProcessingService.splitTransaction(existingTransaction.getId(), subTransactions, securityUser);

        // Then
        // Проверяем, что родительская транзакция скрыта и помечена как мастер
        Transaction parent = entityManager.find(Transaction.class, existingTransaction.getId());
        assertThat(parent.isHide()).isTrue();
        assertThat(parent.isMaster()).isTrue();

        // Проверяем создание подтранзакций
        List<Transaction> subTransactionsList = transactionRepository.findByParentTransaction(parent);
        assertThat(subTransactionsList).hasSize(3);

        assertThat(subTransactionsList)
                .extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Food", "Transport", "Entertainment");

        assertThat(subTransactionsList)
                .extracting(Transaction::getAmount)
                .containsExactlyInAnyOrder(
                        new BigDecimal("-400.00"),
                        new BigDecimal("-300.00"),
                        new BigDecimal("-300.00")
                );
    }

    @Test
    @DisplayName("Должен выбросить исключение при несовпадении сумм при разделении")
    void shouldThrowExceptionWhenSplitAmountsDontMatch() {
        // Given
        List<SubTransactionDto> subTransactions = List.of(
                new SubTransactionDto("Food", new BigDecimal("400.00")),
                new SubTransactionDto("Transport", new BigDecimal("300.00"))
                // Сумма 700.00 вместо 1000.00
        );
        Long existingTransactionId = existingTransaction.getId();
        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> transactionProcessingService.splitTransaction(existingTransactionId, subTransactions, securityUser));
    }

    @Test
    @DisplayName("Должен скрыть транзакцию и обновить баланс")
    void shouldHideTransactionAndUpdateBalance() {
        // Given
        BigDecimal initialBalance = account1.getBalance();
        boolean initialHideState = existingTransaction.isHide();

        // When
        TransactionDto result = transactionProcessingService.hideTransactionWithBalanceUpdate(
                existingTransaction.getId(), securityUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(result.isHide()).isEqualTo(!initialHideState);

        // Проверяем обновление баланса (транзакция была расходной, поэтому при скрытии баланс должен увеличиться)
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(
                initialBalance.add(existingTransaction.getAmount().abs())
        );
    }

    @Test
    @DisplayName("Должен удалить транзакцию и обновить баланс")
    void shouldDeleteTransactionAndUpdateBalance() {
        // Given
        BigDecimal initialBalance = account1.getBalance();
        assertThat(entityManager.find(Transaction.class, existingTransaction.getId())).isNotNull();

        // When
        transactionProcessingService.deleteTransactionWithBalanceUpdate(existingTransaction.getId(), securityUser);

        // Then
        entityManager.flush();
        entityManager.clear();

        // Проверяем, что транзакция удалена
        assertThat(entityManager.find(Transaction.class, existingTransaction.getId())).isNull();

        // Проверяем обновление баланса (транзакция была расходной, поэтому при удалении баланс должен увеличиться)
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(
                initialBalance.add(existingTransaction.getAmount().abs())
        );
    }

    @Test
    @DisplayName("Должен обновить транзакцию и скорректировать баланс при изменении суммы")
    void shouldPatchTransactionAndAdjustBalance() {
        // Given
        BigDecimal initialBalance = account1.getBalance();
        PatchTransactionData patchData = PatchTransactionData.builder()
                .amount(new BigDecimal("-1500.00")) // Было -1000.00
                .categoryId(categorySalary.getId())
                .operationTime(LocalDateTime.now())
                .build();

        // When
        TransactionDto result = transactionProcessingService.patchTransactionWithBalanceUpdate(
                existingTransaction.getId(), patchData, securityUser);

        // Then
        entityManager.flush();
        entityManager.clear();

        assertThat(result.getAmount()).isEqualByComparingTo("-1500.00");
        assertThat(result.getCategoryId()).isEqualTo(categorySalary.getId());

        // Проверяем корректировку баланса (разница -500.00, поэтому баланс должен уменьшиться на 500)
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(
                initialBalance.subtract(new BigDecimal("500.00"))
        );
    }

    @Test
    @DisplayName("Должен обновить транзакцию без изменения баланса при неизменной сумме")
    void shouldPatchTransactionWithoutBalanceChangeWhenAmountUnchanged() {
        // Given
        BigDecimal initialBalance = account1.getBalance();
        PatchTransactionData patchData = PatchTransactionData.builder()
                .amount(existingTransaction.getAmount()) // Та же сумма
                .categoryId(categorySalary.getId())
                .operationTime(LocalDateTime.now())
                .build();

        // When
        TransactionDto result = transactionProcessingService.patchTransactionWithBalanceUpdate(
                existingTransaction.getId(), patchData, securityUser);

        // Then
        assertThat(result.getCategoryId()).isEqualTo(categorySalary.getId());

        // Баланс не должен измениться
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(initialBalance);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать нулевые суммы в подтранзакциях")
    void shouldHandleZeroAmountInSubTransactions() {
        // Given
        List<SubTransactionDto> subTransactions = List.of(
                new SubTransactionDto("Food", new BigDecimal("-600.00")),
                new SubTransactionDto("Zero", BigDecimal.ZERO), // Нулевая сумма
                new SubTransactionDto("Transport", new BigDecimal("-400.00"))
        );

        // When
        transactionProcessingService.splitTransaction(existingTransaction.getId(), subTransactions, securityUser);
        entityManager.flush();
        entityManager.clear();

        // Then
        // Проверяем, что созданы только подтранзакции с ненулевой суммой
        Transaction parent = entityManager.find(Transaction.class, existingTransaction.getId());
        List<Transaction> subTransactionsList = transactionRepository.findByParentTransaction(parent);

        assertThat(subTransactionsList).hasSize(2); // Только 2 подтранзакции (нулевая игнорируется)
        assertThat(subTransactionsList)
                .extracting(Transaction::getDescription)
                .containsExactlyInAnyOrder("Food", "Transport");
    }

    @Test
    @DisplayName("Должен корректно обрабатывать скрытие и раскрытие транзакции")
    void shouldHandleHideAndUnhideTransaction() {
        // Given
        BigDecimal initialBalance = account1.getBalance();

        // First hide
        TransactionDto hiddenResult = transactionProcessingService.hideTransactionWithBalanceUpdate(
                existingTransaction.getId(), securityUser);
        assertThat(hiddenResult.isHide()).isTrue();
        entityManager.flush();
        entityManager.clear();
        BigDecimal afterHideBalance = entityManager.find(BankAccount.class, account1.getId()).getBalance();
        assertThat(afterHideBalance).isNotEqualByComparingTo(initialBalance);


        // When unhide
        TransactionDto unhiddenResult = transactionProcessingService.hideTransactionWithBalanceUpdate(
                existingTransaction.getId(), securityUser);
        // Then
        assertThat(unhiddenResult.isHide()).isFalse();
        // Проверяем, что баланс вернулся к исходному значению
        entityManager.flush();
        entityManager.clear();
        BankAccount finalAccount = entityManager.find(BankAccount.class, account1.getId());
        assertThat(finalAccount.getBalance()).isEqualByComparingTo(initialBalance);
    }
}