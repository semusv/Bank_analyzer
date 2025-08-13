package ru.vvsem.bank.analyzer.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@DisplayName("Репозиторий карт")
class JpaCardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TestEntityManager em;

    private BankAccount user1Acc1;      // первый аккаунт пользователя 1
    private BankAccount user1Acc2;      // второй аккаунт пользователя 1
    private BankAccount user2Acc1;      // аккаунт другого пользователя

    private Bank issuerBank1;          // первый эмитент
    private Bank issuerBank2;          // второй эмитент


    @BeforeEach
    void setUp() {

        User user1 = new User();
        user1.setLogin("john.doe");
        user1.setName("John");
        user1.setSurname("Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("qwerty");
        user1.setTelegramChatId("12345");
        em.persist(user1);

        User user2 = new User();
        user2.setLogin("jane.doe");
        user2.setName("Jane");
        user2.setSurname("Doe");
        user2.setEmail("jane@example.com");
        user2.setPassword("qwerty");
        em.persist(user2);

        Bank mainBank = new Bank();
        mainBank.setName("MainBank");
        mainBank.setBic("111111111");
        em.persist(mainBank);

        Bank altBank = new Bank();
        altBank.setName("AltBank");
        altBank.setBic("222222222");
        em.persist(altBank);

        Currency rub = new Currency();
        rub.setCode("Val");
        rub.setSymbol("V");
        rub.setName("Валюта");
        em.persist(rub);

        issuerBank1 = new Bank();
        issuerBank1.setName("IssuerBank1");
        issuerBank1.setBic("333333333");
        em.persist(issuerBank1);

        issuerBank2 = new Bank();
        issuerBank2.setName("IssuerBank2");
        issuerBank2.setBic("444444444");
        em.persist(issuerBank2);

        user1Acc1 = bankAccount("Account1", "AN1-0001", mainBank, rub, user1);
        user1Acc2 = bankAccount("Account2", "AN1-0002", altBank, rub, user1);
        user2Acc1 = bankAccount("Account3", "AN2-0001", mainBank, rub, user2);

       createCard("1234", "Card_1", user1Acc1, issuerBank1);
       createCard("5678", "Card_2", user1Acc1, issuerBank1);
       createCard("9012", "Card_3", user1Acc2, issuerBank2);
       createCard("3456", "Card_4", user2Acc1, issuerBank2);

        em.flush();
    }

    private BankAccount bankAccount(String name, String number,
                                    Bank bank, Currency currency, User user) {
        BankAccount acc = new BankAccount();
        acc.setName(name);
        acc.setAccountNumber(number);
        acc.setBank(bank);
        acc.setCurrency(currency);
        acc.setUser(user);
        em.persist(acc);
        return acc;
    }

    private void createCard(String last4, String name,
                            BankAccount account, Bank issuer) {
        Card card = new Card();
        card.setLastFourDigits(last4);
        card.setCardName(name);
        card.setAccount(account);
        card.setIssuerBank(issuer);
        em.persist(card);

    }


    @Test
    @DisplayName("Find cards by account")
    void shouldFindByAccount() {
        //given
        //when
        List<Card> cards = cardRepository.findByAccountId(user1Acc1.getId());
        //then
        assertThat(cards).hasSize(2);
        assertThat(cards)
                .extracting(Card::getLastFourDigits)
                .containsExactlyInAnyOrder("1234", "5678");
        assertThat(cards)
                .allMatch(c -> c.getAccount().equals(user1Acc1));
        assertThat(cards)
                .extracting(Card::getIssuerBank)
                .containsOnly(issuerBank1);
    }

    @Test
    @DisplayName("Find cards by issuer bank")
    void shouldFindByIssuerBank() {
        //given
        //when
        List<Card> cards = cardRepository.findByIssuerBankId(issuerBank1.getId());

        //then
        assertThat(cards).hasSize(2);
        assertThat(cards)
                .extracting(Card::getLastFourDigits)
                .containsExactlyInAnyOrder("1234", "5678");
        assertThat(cards)
                .allSatisfy(card -> assertThat(card.getIssuerBank())
                        .isEqualTo(issuerBank1));
    }

    @Test
    @DisplayName("Find cards by user")
    void shouldFindByUser() {
        List<Card> cards = cardRepository.findByAccountUserId(user1Acc1.getUser().getId());
        assertThat(cards).hasSize(3);
        assertThat(cards)
                .extracting(Card::getLastFourDigits)
                .containsExactlyInAnyOrder("1234", "5678", "9012");
    }

    @Test
    @DisplayName("Should return empty list for non‑existing account")
    void shouldReturnEmptyWhenAccountNotExist() {
        List<Card> cards = cardRepository.findByAccountId(999L);
        assertThat(cards).isEmpty();
    }
}