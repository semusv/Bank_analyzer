package ru.vvsem.bank.analyzer.services.security;


import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.vvsem.bank.analyzer.dto.auth.RegisterFormDto;
import ru.vvsem.bank.analyzer.exceptions.RegistrationException;
import ru.vvsem.bank.analyzer.mappers.RegisterFormMapperImpl;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.Role;
import ru.vvsem.bank.analyzer.repositories.BaseRepositoryTest;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({
        CustomUserDetailsService.class,
        PasswordServiceImpl.class,
        BCryptPasswordEncoder.class,
        RegisterFormMapperImpl.class
})
class CustomUserDetailsServiceIntegrationTest extends BaseRepositoryTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordService passwordService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        // Очищаем базу и контекст безопасности перед каждым тестом
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();

        // Создаём существующего пользователя
        existingUser = new User();
        existingUser.setLogin("existinguser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordService.encodePassword("password123"));
        existingUser.setName("Existing");
        existingUser.setSurname("User");
        existingUser.setRoles(Set.of(Role.USER));
        existingUser = entityManager.persistAndFlush(existingUser);

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("Должен загрузить пользователя по username")
    void shouldLoadUserByUsername() {
        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("existinguser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("existinguser");
        assertThat(userDetails.getPassword()).isNotBlank();
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        // Проверяем, что это SecurityUser
        assertThat(userDetails).isInstanceOf(SecurityUser.class);
        SecurityUser securityUser = (SecurityUser) userDetails;
        assertThat(securityUser.getId()).isEqualTo(existingUser.getId());
        assertThat(securityUser.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("Должен выбросить исключение при загрузке несуществующего пользователя")
    void shouldThrowExceptionWhenLoadingNonExistentUser() {
        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent")
        );

        assertThat(exception.getMessage()).isEqualTo("User not found: nonexistent");
    }

    @Test
    @DisplayName("Должен зарегистрировать нового пользователя")
    void shouldRegisterNewUser() {
        // Given
        RegisterFormDto newUser = new RegisterFormDto();
        newUser.setLogin("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("plainpassword");
        newUser.setName("New");
        newUser.setSurname("User");

        // When
        customUserDetailsService.registerUser(newUser);

        // Then
        // Проверяем, что пользователь сохранен в БД
        User savedUser = userRepository.findByLogin("newuser").orElseThrow();
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getLogin()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getName()).isEqualTo("New");
        assertThat(savedUser.getSurname()).isEqualTo("User");

        // Проверяем, что пароль захеширован
        assertThat(savedUser.getPassword()).isNotEqualTo("plainpassword");
        assertThat(passwordService.matches("plainpassword", savedUser.getPassword())).isTrue();

        // Проверяем, что роль установлена
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
    }

    @Test
    @DisplayName("Должен выбросить исключение при регистрации с существующим логином")
    void shouldThrowExceptionWhenRegisteringWithExistingLogin() {
        // Given
        RegisterFormDto duplicateUser = new RegisterFormDto();
        duplicateUser.setLogin("existinguser"); // Существующий логин
        duplicateUser.setEmail("different@example.com");
        duplicateUser.setPassword("password");
        duplicateUser.setName("Duplicate");
        duplicateUser.setSurname("User");

        // When & Then
        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> customUserDetailsService.registerUser(duplicateUser)
        );

        assertThat(exception.getMessage()).isEqualTo("User with login existinguser already exists");
    }

    @Test
    @DisplayName("Должен выбросить исключение при регистрации с существующим email")
    void shouldThrowExceptionWhenRegisteringWithExistingEmail() {
        // Given
        RegisterFormDto duplicateUser = new RegisterFormDto();
        duplicateUser.setLogin("differentuser");
        duplicateUser.setEmail("existing@example.com"); // Существующий email
        duplicateUser.setPassword("password");
        duplicateUser.setName("Duplicate");
        duplicateUser.setSurname("User");

        // When & Then
        RegistrationException exception = assertThrows(
                RegistrationException.class,
                () -> customUserDetailsService.registerUser(duplicateUser)
        );

        assertThat(exception.getMessage()).isEqualTo("User with email existing@example.com already exists");
    }

    @Test
    @DisplayName("Должен установить роль USER по умолчанию")
    void shouldSetDefaultUserRole() {
        // Given
        RegisterFormDto newUser = new RegisterFormDto();
        newUser.setLogin("defaultroleuser");
        newUser.setEmail("defaultrole@example.com");
        newUser.setPassword("password");
        newUser.setName("Default");
        newUser.setSurname("Role");


        // When
        customUserDetailsService.registerUser(newUser);

        // Then
        User savedUser = userRepository.findByLogin("defaultroleuser").orElseThrow();
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
    }

    @Test
    @DisplayName("Должен получить текущего аутентифицированного пользователя")
    void shouldGetCurrentAuthenticatedUser() {
        // Given
        SecurityUser securityUser = new SecurityUser(
                existingUser.getId(),
                existingUser.getLogin(),
                existingUser.getPassword(),
                existingUser.getAuthorities(),
                true, true, true, true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(securityUser);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When
        User currentUser = customUserDetailsService.getCurrentUser();

        // Then
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo(existingUser.getId());
        assertThat(currentUser.getLogin()).isEqualTo(existingUser.getLogin());
        assertThat(currentUser.getEmail()).isEqualTo(existingUser.getEmail());
    }

    @Test
    @DisplayName("Должен выбросить исключение когда пользователь не аутентифицирован")
    void shouldThrowExceptionWhenUserNotAuthenticated() {
        // Given - очищенный контекст безопасности

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> customUserDetailsService.getCurrentUser()
        );

        assertThat(exception.getMessage()).isEqualTo("Пользователь не аутентифицирован");
    }

    @Test
    @DisplayName("Должен выбросить исключение когда principal является anonymousUser")
    void shouldThrowExceptionWhenPrincipalIsAnonymousUser() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> customUserDetailsService.getCurrentUser()
        );

        assertThat(exception.getMessage()).isEqualTo("Пользователь не аутентифицирован");
    }

    @Test
    @DisplayName("Должен получить пользователя по ID")
    void shouldGetUserById() {
        // When
        User user = customUserDetailsService.getUserById(existingUser.getId());

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(existingUser.getId());
        assertThat(user.getLogin()).isEqualTo(existingUser.getLogin());
        assertThat(user.getEmail()).isEqualTo(existingUser.getEmail());
    }

    @Test
    @DisplayName("Должен выбросить исключение при получении несуществующего пользователя по ID")
    void shouldThrowExceptionWhenGettingNonExistentUserById() {
        // Given
        Long nonExistentId = 999L;

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.getUserById(nonExistentId)
        );

        assertThat(exception.getMessage()).isEqualTo("User not found: 999");
    }

    @Test
    @DisplayName("Должен обработать аутентификацию с именем вместо UserDetails")
    void shouldHandleAuthenticationWithNameInsteadOfUserDetails() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("existinguser"); // String вместо UserDetails
        when(authentication.getName()).thenReturn("existinguser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When
        User currentUser = customUserDetailsService.getCurrentUser();

        // Then
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getLogin()).isEqualTo("existinguser");
    }

    @Test
    @DisplayName("Должен сохранить пользователя с существующими ролями")
    void shouldPreserveExistingRolesWhenRegistering() {
        // Given
        User adminUser = new User();
        adminUser.setLogin("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setName("Admin");
        adminUser.setSurname("User");
        adminUser.setRoles(Set.of(Role.ADMIN, Role.USER)); // Явно устанавливаем роли

        // When
        customUserDetailsService.registerUser(adminUser);

        // Then
        User savedUser = userRepository.findByLogin("adminuser").orElseThrow();
        assertThat(savedUser.getRoles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    @DisplayName("Должен выдать исключение при регистрации пользователя с пустыми полями")
    void shouldHandleUserWithEmptyFields() {
        // Given
        User minimalUser = new User();
        minimalUser.setLogin("minimaluser");
        minimalUser.setEmail("minimal@example.com");
        minimalUser.setPassword("pass");
        // name и surname не установлены

        // When
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> customUserDetailsService.registerUser(minimalUser)
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getConstraintViolations())
                .hasSize(2)
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "surname");

    }
}