package ru.vvsem.bank.analyzer.services;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.vvsem.bank.analyzer.mappers.UserMapperImpl;

@DataJpaTest
@DisplayName("Интеграционный тест для Юзеров")
@Import({ UserServiceImpl.class,
        UserMapperImpl.class})
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Должен найти юзера по логину")
    void shouldFindUserByLogin() {
        if (userService.getUserByLogin("login_1") != null) {
            System.out.println("Юзер найден");
        } else {
            System.out.println("Юзер не найден");
        }
    }
}