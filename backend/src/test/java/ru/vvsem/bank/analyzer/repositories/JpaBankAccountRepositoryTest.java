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
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application.yml")
class JpaBankAccountRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final List<User> userList  = new ArrayList<>();
    private Currency rub;
    private Currency usd;
    private Bank sber;

    @BeforeEach
    void setUp() {
        rub = new Currency("CU1", "₽", "Russian Ruble");
        usd = new Currency("CU2", "$", "US Dollar");
        entityManager.persistAndFlush(rub);
        entityManager.persistAndFlush(usd);

        sber = new Bank();
        sber.setName("Sberbank");
        sber.setBankCode("sber");
        sber.setBic("11111111");
        entityManager.persistAndFlush(sber);

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
    }

    @Test
    @DisplayName("Должен обновить баланс счёта по ID")
    void shouldUpdateBalance_WhenValidId() {
        // Given
        BankAccount account = createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", usd, BigDecimal.valueOf(1000));
        BigDecimal newBalance = BigDecimal.valueOf(1500);
        entityManager.detach(account);

        // When
        int updatedRows = bankAccountRepository.updateBalance(newBalance, account.getId());

        // Then
        assertThat(updatedRows).isEqualTo(1);
        BankAccount updatedAccount = entityManager.find(BankAccount.class, account.getId());
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(newBalance);
    }

    @Test
    @DisplayName("Должен найти счёт по ID и userId")
    void shouldFindByIdAndUserId_WhenAccountExists() {
        // Given
        BankAccount account = createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", usd, BigDecimal.valueOf(1000));

        // When
        Optional<BankAccount> result = bankAccountRepository.findByIdAndUserId(
                account.getId(), userList.get(0).getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAccountNumber()).isEqualTo("40817810099910000001");
    }

    @Test
    @DisplayName("Должен вернуть пустой Optional, если счёт не принадлежит пользователю")
    void shouldNotFindByIdAndUserId_WhenWrongUser() {
        // Given
        BankAccount account = createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", usd, BigDecimal.valueOf(1000));
        Long otherUserId = 999L;

        // When
        Optional<BankAccount> result = bankAccountRepository.findByIdAndUserId(account.getId(), otherUserId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен найти все счета пользователя")
    void shouldFindByUserId_ReturnAllAccountsForUser() {
        // Given
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", usd, BigDecimal.valueOf(1000));
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000002", rub, BigDecimal.valueOf(2000));
        createAndPersistBankAccount(
                userList.get(1), "40817810099910000003", rub, BigDecimal.valueOf(5000));
        // When
        List<BankAccount> result = bankAccountRepository.findByUserId(userList.get(0).getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("accountNumber")
                .containsExactlyInAnyOrder("40817810099910000001", "40817810099910000002");
    }


    @Test
    @DisplayName("Должен рассчитать общий баланс по валютам для пользователя")
    void shouldCalculateTotalBalanceByUserId_ReturnSumGroupedByCurrency() {
        // Given
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", rub, BigDecimal.valueOf(1000));
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000002", rub, BigDecimal.valueOf(2000));
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000003", usd, BigDecimal.valueOf(500));

        // When
        List<CurrencyAmountDto> balances = bankAccountRepository.calculateTotalBalanceByUserId(userList.get(0).getId());

        // Then
        assertThat(balances).hasSize(2);
        assertThat(balances)
                .filteredOn(dto -> "CU1".equals(dto.getCurrencyCode()))
                .first()
                .satisfies(dto -> assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000)));
        assertThat(balances)
                .filteredOn(dto -> "CU2".equals(dto.getCurrencyCode()))
                .first()
                .satisfies(dto -> assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500)));
    }

    @Test
    @DisplayName("Должен найти все счета с подгрузкой связанных сущностей (cards, user, currency, bank)")
    void shouldFindWithCardsAndUserAndCurrencyByUserId_ReturnAccountsWithEagerFetch() {
        // Given
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000001", rub, BigDecimal.valueOf(1000));
        createAndPersistBankAccount(
                userList.get(0), "40817810099910000002", usd, BigDecimal.valueOf(2000));

        // When
        List<BankAccount> result = bankAccountRepository.findWithCardsAndUserAndCurrencyByUserId(
                userList.get(0).getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("accountNumber")
                .containsExactlyInAnyOrder("40817810099910000001", "40817810099910000002");

        // Проверяем, что связанные сущности загружены
        assertThat(result.get(0).getUser()).isNotNull();
        assertThat(result.get(0).getCurrency()).isNotNull();
        assertThat(result.get(0).getBank()).isNotNull();
        assertThat(result.get(0).getCards()).isNotNull();
    }

    // Хелпер
    private BankAccount createAndPersistBankAccount(User user, String accountNumber, Currency currency, BigDecimal balance) {
        BankAccount account = new BankAccount();
        account.setName("Test Account");
        account.setUser(user);
        account.setAccountNumber(accountNumber);
        account.setCurrency(currency);
        account.setBalance(balance);
        account.setBank(sber);
        return entityManager.persistAndFlush(account);
    }
}