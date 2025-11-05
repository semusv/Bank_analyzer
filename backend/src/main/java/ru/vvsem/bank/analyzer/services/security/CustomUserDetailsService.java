package ru.vvsem.bank.analyzer.services.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordService passwordService;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new SecurityUser(
                user.getId(),
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities(),
                true, // accountNonExpired
                true, // accountNonLocked
                true, // credentialsNonExpired
                true  // enabled
        );

    }

    @Transactional
    public void registerUser(User user) {
        log.info("Registering new user: {}", user.getLogin());

        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalArgumentException("User with login " + user.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        user.setPassword(passwordService.encodePassword(user.getPassword()));

        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof SecurityUser securityUser) {
            // Получаем User из БД по ID из SecurityUser
            return userRepository.findById(securityUser.getId())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + securityUser.getId()));
        } else if (principal instanceof String username) {
            return userRepository.findByLogin(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } else {
            throw new UsernameNotFoundException("Unsupported principal type: " + principal.getClass().getName());
        }
    }

    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }
}
