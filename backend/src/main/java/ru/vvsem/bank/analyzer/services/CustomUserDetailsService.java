package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

    private final RequestToViewNameTranslator requestToViewNameTranslator;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user;
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                user.getAuthorities()
//        );
    }

    @Transactional
    public User registerUser(User user) {
        log.info("Registering new user: {}", user.getLogin());

        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("User with login " + user.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        user.setPassword(passwordService.encodePassword(user.getPassword()));

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        // Этот метод будет использоваться вместе с SecurityContext
        // Пока возвращаем тестового пользователя
        return userRepository.findById(1L)
                .orElseThrow(() -> new UsernameNotFoundException("Default user not found"));
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

}
