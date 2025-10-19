package ru.vvsem.bank.analyzer.repositories;

import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@DataJpaTest
@DisplayName("Репозиторий транзакций")
class JpaTransactionRepositoryTest {
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private TestEntityManager em;
//
//    private User user1;
//    private User user2;
//    private Currency rub;
//    private Currency usd;
//    private Currency eur;
//
//
//    @BeforeEach
//    void setUp() {
//        user1 = user("user001");
//        user2 = user("user002");
//
//        rub = currency("Va1", "Z", "Валюта 1");
//        usd = currency("Va2", "X", "Валюта 2");
//        eur = currency("Va3", "V", "Валюта 3");
//
//        transaction(user1, rub, "Покупка", BigDecimal.valueOf(100), LocalDateTime.now().minusDays(3));
//        transaction(user1, usd, "Возврат", BigDecimal.valueOf(-50), LocalDateTime.now().minusDays(2));
//        transaction(user1, rub, "Капитализация", BigDecimal.valueOf(200), LocalDateTime.now().minusHours(5));
//
//        transaction(user2, eur, "Покупка", BigDecimal.valueOf(500), LocalDateTime.now().minusDays(1));
//        transaction(user2, rub, "Отчисление", BigDecimal.valueOf(-300), LocalDateTime.now().minusHours(1));
//
//        em.flush();
//    }
//
//
//    private User user(String login) {
//        User u = new User();
//        u.setLogin(login);
//        u.setName("Name");
//        u.setSurname("Surname");
//        u.setEmail(login + "@example.com");
//        u.setPassword("pass");
//        em.persist(u);
//        return u;
//    }
//
//    private Currency currency(String code, String sym, String name) {
//        Currency c = new Currency();
//        c.setCode(code);
//        c.setSymbol(sym);
//        c.setName(name);
//        em.persist(c);
//        return c;
//    }
//
//    private void transaction(User u, Currency c, String desc,
//                                    BigDecimal amount, LocalDateTime time) {
//        Transaction tr = new Transaction();
//        tr.setUser(u);
//        tr.setCurrency(c);
//        tr.setDescription(desc);
//        tr.setAmount(amount);
//        tr.setOperationTime(time);
//        em.persist(tr);
//    }
//
//    @Test
//    @DisplayName("Найти все транзакции по пользователю")
//    void shouldFindAllByUser() {
//        List<Transaction> list = transactionRepository.findByUserId(user1.getId());
//        assertThat(list).hasSize(3)
//                .extracting(Transaction::getDescription)
//                .containsOnly("Покупка", "Возврат", "Капитализация");
//    }
//
//
//    @Test
//    @DisplayName("Найти транзакции по подстроке в описании")
//    void shouldFindByDescriptionContainingIgnoreCase() {
//        List<Transaction> list = transactionRepository
//                .findByDescriptionContainingIgnoreCase("покупка");
//        assertThat(list).hasSize(2) // t1 (Покупка) + t4 (Покупка)
//                .extracting(Transaction::getUser)
//                .extracting(User::getLogin)
//                .containsExactlyInAnyOrder("user001", "user002");
//    }
//
//    @Test
//    @DisplayName("Найти все положительные транзакции > 0")
//    void shouldFindByAmountGreaterThanZero() {
//        List<Transaction> pos = transactionRepository.findByAmountGreaterThan(new BigDecimal("0"));
//        assertThat(pos).hasSize(3)
//                .extracting(Transaction::getAmount)
//                .doesNotContain(BigDecimal.valueOf(-50), BigDecimal.valueOf(-300));
//    }
//
//    @Test
//    @DisplayName("Найти все транзакции по валюте")
//    void shouldFindByCurrency() {
//        List<Transaction> rubList = transactionRepository.findByCurrencyCode(rub.getCode());
//        assertThat(rubList).hasSize(3)                   // t1, t3, t5
//                .extracting(Transaction::getUser)
//                .extracting(User::getLogin)
//                .containsExactlyInAnyOrder("user001", "user002","user001" );
//    }
//
//
//
//    @Test
//    @DisplayName("Найти транзакции между датами и пользователем одновременно")
//    void shouldFindByUserAndDateRange() {
//        LocalDateTime start = LocalDateTime.now().minusDays(2);
//        LocalDateTime end = LocalDateTime.now();
//        List<Transaction> list = transactionRepository
//                .findByUserIdAndOperationTimeBetween(user2.getId(), start, end);
//
//        assertThat(list).hasSize(2)                     // t4, t5
//                .extracting(Transaction::getDescription)
//                .containsExactlyInAnyOrder("Покупка", "Отчисление");
//    }
//
//    @Test
//    @DisplayName("Найти транзакции между датами и пользователем одновременно")
//    void shouldFindByUserAndDateRange2() {
//        LocalDateTime start = LocalDateTime.now().minusDays(2);
//        LocalDateTime end = LocalDateTime.now().minusHours(2);
//        List<Transaction> list = transactionRepository
//                .findByUserIdAndOperationTimeBetween(user2.getId(), start, end);
//
//        assertThat(list).hasSize(1)                     // t4, t5
//                .extracting(Transaction::getDescription)
//                .containsExactlyInAnyOrder("Покупка");
//    }
//
//    @Test
//    @DisplayName("Ни одной транзакции не должно быть для пользователя с id = 999")
//    void shouldReturnEmptyWhenNoTransactionForUser() {
//        User ghost = new User();
//        ghost.setId(999L);
//        List<Transaction> list = transactionRepository.findByUserId(ghost.getId());
//        assertThat(list).isEmpty();
//    }
}