package ru.vvsem.bank.analyzer.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Репозиторий счетов банка")
class JpaBankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository accountRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    private TestEntityManager em;

    private BankAccount testAccount;

    private User user;
    private Bank bank;
    private Currency currency;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setLogin("john.doe");
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john@example.com");
        user.setPassword("qwerty");
        user.setTelegramChatId("12345");
        em.persist(user);

        bank = new Bank();
        bank.setName("Bank");
        bank.setBic("123456789");
        em.persist(bank);

        currency = new Currency();
        currency.setCode("VAL");
        currency.setSymbol("V");
        currency.setName("Валюта");
        em.persist(currency);

        testAccount = new BankAccount();
        testAccount.setName("Основной счет");
        testAccount.setAccountNumber("40817810099910004321");
        testAccount.setBank(bank);
        testAccount.setCurrency(currency);
        testAccount.setUser(user);
        em.persist(testAccount);
        em.flush();
    }

    @Test
    @DisplayName("Find account by account number")
    void shouldFindByAccountNumber() {
        //given
        //when
        Optional<BankAccount> found = accountRepository.findByAccountNumber(testAccount.getAccountNumber());
        //then
        assertThat(found).isPresent().get().satisfies(account -> {
            assertThat(account.getName()).isEqualTo(testAccount.getName());
            assertThat(account.getAccountNumber()).isEqualTo(testAccount.getAccountNumber());
        });
    }

    @Test
    @DisplayName("Find accounts by user")
    void shouldFindByUser() {
        //given
        //when
        List<BankAccount> accounts = accountRepository.findByUserId(user.getId());
        //then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo(testAccount.getAccountNumber());
    }
    @Test
    @DisplayName("Find accounts by ID")
    void shouldFindById() {
        //given
        //when
        Optional<BankAccount> account = accountRepository.findById(testAccount.getId());
        //then
        assertThat(account).isPresent().get().satisfies(acc -> {
            assertThat(acc.getName()).isEqualTo(testAccount.getName());
            assertThat(acc.getAccountNumber()).isEqualTo(testAccount.getAccountNumber());
        });
    }

    @Test
    @DisplayName("Insert account")
    void shouldCreateBankAccount() {
        //given
        BankAccount newAccount ;
        newAccount = new BankAccount();
        newAccount.setName("Основной счет_2");
        newAccount.setAccountNumber("40817810099910009999");
        newAccount.setBank(bank);
        newAccount.setCurrency(currency);
        newAccount.setUser(user);
        //when
        accountRepository.save(newAccount);
        em.flush();
        em.clear();

        //then
        BankAccount foundAccount = em.find(BankAccount.class, newAccount.getId());
        assertThat(foundAccount).satisfies(account -> {
            assertThat(account.getName()).isEqualTo(newAccount.getName());
            assertThat(account.getAccountNumber()).isEqualTo(newAccount.getAccountNumber());
            assertThat(account.getId()).isEqualTo(newAccount.getId());
            assertThat(account.getUser().getLogin()).isEqualTo(newAccount.getUser().getLogin());
            assertThat(account.getUser().getId()).isEqualTo(newAccount.getUser().getId());
            assertThat(account.getBank().getId()).isEqualTo(newAccount.getBank().getId());
            assertThat(account.getCurrency().getId()).isEqualTo(newAccount.getCurrency().getId());
        });

    }


}