package ru.vvsem.bank.analyzer.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.auth.RegisterFormDto;
import ru.vvsem.bank.analyzer.exceptions.RegistrationException;
import ru.vvsem.bank.analyzer.mappers.RegisterFormMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.Role;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    private final RegisterFormMapper registerFormMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new SecurityUser(
                user.getId(),
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities(),
                true,
                true,
                true,
                true
        );
    }

    @Transactional
    public void registerUser(RegisterFormDto registerFormDto) {
        User user = registerFormMapper.toEntity(registerFormDto);

        registerInDb(user);
    }

    @Transactional
    public void registerUser(User user) {

        registerInDb(user);
    }

    private void registerInDb(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new RegistrationException(
                    "User with login %s already exists".formatted(user.getLogin()),
                    "registration.login.already.exist");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RegistrationException(
                    "User with email %s already exists".formatted(user.getEmail()),
                    "registration.email.already.exist");
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(java.util.Set.of(Role.USER));
        }
        user.setPassword(passwordService.encodePassword(user.getPassword()));
        userRepository.save(user);
    }


    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication.getPrincipal() instanceof String principal && "anonymousUser".equals(principal)) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }

        String login;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            login = userDetails.getUsername();
        } else {
            login = authentication.getName();
        }

        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Текущий пользователь не найден в базе"));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}
