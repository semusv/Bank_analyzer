package ru.vvsem.bank.analyzer.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.User;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@DisplayName("Репозиторий для работы с категориями")
class JpaCategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager em;

    private Category testCategory;

    private User user;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setLogin("john.doe");
        user1.setName("John");
        user1.setSurname("Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("qwerty");
        user1.setTelegramChatId("12345");
        user = user1;
        em.persist(user1);

        User user2 = new User();
        user2.setLogin("jane.doe");
        user2.setName("Jane");
        user2.setSurname("Doe");
        user2.setEmail("jane@example.com");
        user2.setPassword("qwerty");
        em.persist(user2);


        testCategory = new Category();
        testCategory.setName("Еда");
        testCategory.setColor("#FF0000");
        testCategory.setUser(user);
        em.persist(testCategory);

        testCategory = new Category();
        testCategory.setName("Транспорт");
        testCategory.setColor("#46FFFA");
        testCategory.setUser(user);
        em.persist(testCategory);


        testCategory = new Category();
        testCategory.setName("Транспорт");
        testCategory.setColor("#46FFFA");
        testCategory.setUser(user2);
        em.persist(testCategory);
        em.flush();
    }

    @Test
    @DisplayName("Find categories by user")
    void shouldFindByUser() {
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        assertThat(categories).hasSize(2);
        assertThat(categories)
                .extracting(Category::getName)
                        .containsExactlyInAnyOrder("Еда", "Транспорт");
        assertThat(categories)
                .allMatch(c -> c.getUser().equals(user));
    }

}