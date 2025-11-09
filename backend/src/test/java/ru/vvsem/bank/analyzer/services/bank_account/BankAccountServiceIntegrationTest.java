package ru.vvsem.bank.analyzer.services.bank_account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapperImpl;
import ru.vvsem.bank.analyzer.mappers.CardMapperImpl;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.Role;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.UserRepository;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DataJpaTest
@Testcontainers
@TestPropertySource("classpath:application-test.yml")
@Import({
        BankAccountServiceImpl.class,
        BankAccountMapperImpl.class,
        CardMapperImpl.class,
})
class BankAccountServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EntityAccessProvider entityAccessProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private TestEntityManager entityManager;

    private SecurityUser securityUser;
    private User user;
    private Bank bank;
    private Currency currency;

    @BeforeEach
    void setUp() {
        // Создаём реальные сущности в БД
        currency = new Currency("CU1", "₽", "Russian Ruble");
        entityManager.persistAndFlush(currency);

        bank = new Bank();
        bank.setName("Sberbank");
        bank.setBankCode("sber");
        bank.setBic("11111111");
        bank = entityManager.persistAndFlush(bank);

        user = new User();
        user.setLogin("testuser");
        user.setEmail("test@example.com");
        user.setPassword("pass");
        user.setName("Test");
        user.setSurname("User");
        user.setRoles(Set.of(Role.USER));
        user = userRepository.saveAndFlush(user);

        // Настраиваем моки
        securityUser = new SecurityUser(
                user.getId(), user.getLogin(), user.getPassword(), user.getAuthorities(),
                true, true, true, true);
        when(customUserDetailsService.getUserById(user.getId())).thenReturn(user);
        when(entityAccessProvider.requireBank(bank.getId())).thenReturn(bank);
        when(entityAccessProvider.requireCurrency(currency.getId())).thenReturn(currency);
        when(entityAccessProvider.requireOwnedBankAccount(1L, user.getId())).thenAnswer(invocation -> {
            var account = bankAccountRepository.findById(1L).orElse(null);
            if (account == null || !account.getUser().getId().equals(user.getId())) {
                throw new EntityNotFoundException("Not found", "code");
            }
            return account;
        });
    }

    @Test
    @DisplayName("Должен создать счёт и сохранить его в БД")
    void shouldCreateBankAccount() {
        // Given
        var dto = new NewBankAccountDto();
        dto.setAccountNumber("40817810099910000001");
        dto.setBankId(bank.getId());
        dto.setCurrencyId(currency.getId());
        dto.setName("Main Account");
        dto.setInitialBalance(BigDecimal.valueOf(1000.0));

        // When
        var result = bankAccountService.createAccount(dto, securityUser);

        // Then
        assertThat(result.getAccountNumber()).isEqualTo("40817810099910000001");
        assertThat(result.getName()).isEqualTo("Main Account");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000.0));

        var saved = bankAccountRepository.findById(result.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getAccountNumber()).isEqualTo("40817810099910000001");
    }

    @ParameterizedTest
    @ValueSource(strings = {"300", "500", "1000", "-200"})
    @DisplayName("Должен обновить баланс счёта")
    void shouldUpdateAccountBalance(String amount) {
        BigDecimal addAmount = new BigDecimal(amount);
        BigDecimal initialBalance = BigDecimal.valueOf(500);
        BigDecimal expectedBalance = initialBalance.add(addAmount);


        // Given
        var account = new ru.vvsem.bank.analyzer.models.BankAccount();
        account.setName("Salary");
        account.setAccountNumber("40817810099910000001");
        account.setBalance(initialBalance);
        account.setUser(user);
        account.setCurrency(currency);
        account.setBank(bank);
        var saved = bankAccountRepository.saveAndFlush(account);
        entityManager.detach(account);

        // When
        when(entityAccessProvider.requireOwnedBankAccount(account.getId(), user.getId())).thenReturn(saved);
        bankAccountService.addAccountBalance(saved.getId(), addAmount, securityUser);

        // Then
        var updated = bankAccountRepository.findById(saved.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getBalance()).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего счёта")
    void shouldThrowWhenUpdatingNonExistingAccount() {
        // Given
        when(entityAccessProvider.requireOwnedBankAccount(999L, user.getId()))
                .thenThrow(new EntityNotFoundException("Not found", "code"));

        // When & Then
        assertThatThrownBy(() -> bankAccountService.addAccountBalance(999L, BigDecimal.TEN, securityUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Должен получить все счета пользователя")
    void shouldGetUserAccounts() {
        // Given
        var account1 = new ru.vvsem.bank.analyzer.models.BankAccount();
        account1.setName("Acc1");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setUser(user);
        account1.setCurrency(currency);
        account1.setBank(bank);

        var account2 = new ru.vvsem.bank.analyzer.models.BankAccount();
        account2.setName("Acc2");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(BigDecimal.valueOf(200));
        account2.setUser(user);
        account2.setCurrency(currency);
        account2.setBank(bank);

        bankAccountRepository.saveAllAndFlush(List.of(account1, account2));

        // When
        var result = bankAccountService.getUserBankAccount(user.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("accountNumber")
                .containsExactlyInAnyOrder("40817810099910000001", "40817810099910000002");
    }

    @Test
    @DisplayName("Должен удалить счёт по ID")
    void shouldDeleteAccount() {
        // Given
        var account = new ru.vvsem.bank.analyzer.models.BankAccount();
        account.setName("To be deleted");
        account.setAccountNumber("40817810099910000009");
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setCurrency(currency);
        account.setBank(bank);
        var saved = bankAccountRepository.saveAndFlush(account);

        // When
        when(entityAccessProvider
                .requireOwnedBankAccount(account.getId(), user.getId())).thenReturn(saved);
        bankAccountService.deleteAccount(saved.getId(), securityUser);

        // Then
        assertThat(bankAccountRepository.findById(saved.getId())).isEmpty();
    }
    @Test
    @DisplayName("Должен вернуть все счета с картами, если у пользователя есть счета")
    void shouldReturnAllAccountsWithCards_WhenUserHasAccounts() {
        // Given
        var account1 = new ru.vvsem.bank.analyzer.models.BankAccount();
        account1.setName("Salary Account");
        account1.setAccountNumber("40817810099910000001");
        account1.setBalance(BigDecimal.valueOf(1000));
        account1.setUser(user);
        account1.setCurrency(currency);
        account1.setBank(bank);

        var account2 = new ru.vvsem.bank.analyzer.models.BankAccount();
        account2.setName("Savings Account");
        account2.setAccountNumber("40817810099910000002");
        account2.setBalance(BigDecimal.valueOf(5000));
        account2.setUser(user);
        account2.setCurrency(currency);
        account2.setBank(bank);

        bankAccountRepository.saveAllAndFlush(List.of(account1, account2));

        // When
        List<BankAccountSimpleDto> result = bankAccountService.getUserAccountsWithCards(securityUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("accountNumber")
                .containsExactlyInAnyOrder("40817810099910000001", "40817810099910000002");
        assertThat(result.get(0).getCards()).isNotNull(); // даже если пусто — список инициализирован
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если у пользователя нет счетов")
    void shouldReturnEmptyList_WhenUserHasNoAccounts() {
        // When
        List<BankAccountSimpleDto> result = bankAccountService.getUserAccountsWithCards( securityUser);

        // Then
        assertThat(result).isEmpty();
    }
}