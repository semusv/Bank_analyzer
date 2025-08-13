package ru.vvsem.bank.analyzer.repositories;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.Budget;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Репозиторий для работы с бюджетами")
class JpaBudgetRepositoryTest {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TestEntityManager em;

    private Budget testBudget;

    private User testUser;

    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setLogin("john.doe");
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("qwerty");
        testUser.setTelegramChatId("12345");
        em.persist(testUser);

        var category = new Category();
        category.setName("Food");
        category.setUser(testUser);
        em.persist(category);

        testBudget = new Budget();
        testBudget.setLimitAmount(new BigDecimal("10000.00"));
        testBudget.setUser(testUser);
        testBudget.setCategory(category);
        em.persist(testBudget);
        em.flush();
    }


    @DisplayName("Find budget by user and category")
    @Test
    void shouldFindByUserAndCategory() {
        //given

        //when
        Optional<Budget> found = budgetRepository.findByUserIdAndCategoryId(
                testBudget.getUser().getId(),
                testBudget.getCategory().getId()
        );
        //then
        assertThat(found).isPresent().get().satisfies(budget -> {
            assertThat(budget.getLimitAmount()).isEqualTo(testBudget.getLimitAmount());
            assertThat(budget.getId()).isEqualTo(testBudget.getId());
        });
    }


    @Test
    @DisplayName("Find all budgets by user")
    void shouldFindAllByUser() {
        //given
        var category = new Category();
        category.setName("Transport");
        category.setUser(testUser);
        em.persist(category);

        testBudget = new Budget();
        testBudget.setLimitAmount(new BigDecimal("90000.00"));
        testBudget.setUser(testUser);
        testBudget.setCategory(category);
        em.persist(testBudget);
        em.flush();

        List<BigDecimal> expectedLimits = List.of(
                new BigDecimal("10000.00"),
                new BigDecimal("90000.00")
        );

        //when
        List<Budget> foundBudgets = budgetRepository.findByUserId(testBudget.getUser().getId());
        //then
        assertThat(foundBudgets).hasSize(2);
        foundBudgets.forEach(budget -> {
            assertThat(budget.getUser().getId()).isEqualTo(testUser.getId());
            assertThat(expectedLimits).contains(budget.getLimitAmount());
            assertThat(budget.getCategory().getName()).isIn("Food", "Transport");
        });

    }


}