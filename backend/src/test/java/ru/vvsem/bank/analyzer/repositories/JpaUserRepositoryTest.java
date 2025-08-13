package ru.vvsem.bank.analyzer.repositories;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Репозиторий пользователей")
class JpaUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    TestEntityManager em;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new User();
        testUser.setLogin("john.doe");
        testUser.setName("John");
        testUser.setSurname("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("qwerty");
        testUser.setTelegramChatId("12345");
        em.persist(testUser);

        em.flush();
    }

    @Test
    @DisplayName("findByLogin returns user with full entity graph")
    void shouldFindByLoginLoadsGraph() {
        //given
        em.detach(testUser);

        //when
        Optional<User> opt = userRepository.findByLogin(testUser.getLogin());

        //then
        assertThat(opt).isPresent().get().satisfies(user -> {
            assertThat(user.getLogin()).isEqualTo(testUser.getLogin());
            assertThat(user.getName()).isEqualTo(testUser.getName());
            assertThat(user.getSurname()).isEqualTo(testUser.getSurname());
            assertThat(user.getEmail()).isEqualTo(testUser.getEmail());

        });
    }

    @Test
    @DisplayName("Create new user")
    void shouldCreateUser() {
        //given
        User newUser = new User();
        newUser.setLogin("jane.doe");
        newUser.setName("Jane");
        newUser.setSurname("Doe");
        newUser.setEmail("jane@example.com");
        newUser.setPassword("password");
        newUser.setTelegramChatId("54321");

        //when
        User savedUser = userRepository.save(newUser);
        em.flush();
        em.clear();

        //then
        User foundUser = em.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getLogin()).isEqualTo(newUser.getLogin());
    }

    @Test
    @DisplayName("Update user")
    void shouldUpdateUser() {
        //given
        testUser.setName("John Updated");
        testUser.setEmail("updated@example.com");

        //when
        userRepository.save(testUser);
        em.flush();
        em.clear();

        //then
        User updatedUser = em.find(User.class, testUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("John Updated");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("Delete user")
    void shouldDeleteUser() {
        //when
        userRepository.delete(testUser);
        em.flush();
        em.clear();

        //then
        User deletedUser = em.find(User.class, testUser.getId());
        assertThat(deletedUser).isNull();
    }

    @Test
    @DisplayName("Find all users")
    void shouldFindAllUsers() {
        //given
        User user2 = new User();
        user2.setLogin("jane.doe");
        user2.setName("Jane");
        user2.setSurname("Doe");
        user2.setEmail("jane@example.com");
        user2.setPassword("password");
        user2.setTelegramChatId("54321");
        em.persist(user2);
        em.flush();

        //when
        List<User> users = userRepository.findAll();

        //then
        assertThat(users).isInstanceOf(List.class).hasSize(6);
        assertThat(users).extracting(User::getLogin)
                .containsExactlyInAnyOrder("john.doe", "jane.doe", "login_1" , "login_2" , "login_3", "login_4");
    }
}



