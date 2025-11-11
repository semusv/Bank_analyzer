package ru.vvsem.bank.analyzer.services.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.Role;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;

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
    public void registerUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("User with login " + user.getLogin() + " already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(java.util.Set.of(ru.vvsem.bank.analyzer.models.enums.Role.USER));
        }

        user.setPassword(passwordService.encodePassword(user.getPassword()));

        user.setRoles(Set.of(Role.USER));
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
