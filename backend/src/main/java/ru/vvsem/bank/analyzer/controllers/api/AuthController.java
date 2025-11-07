package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;
import ru.vvsem.bank.analyzer.utils.security.JwtTokenUtil;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);

            // Устанавливаем токен в куку
            Cookie cookie = new Cookie("JWT_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);  // Только HTTP
            cookie.setPath("/");
            cookie.setMaxAge(20); //
            response.addCookie(cookie);

            // Возвращаем JSON для совместимости, но кука — основной способ
            return ResponseEntity.ok(Map.of("success", true));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Неверный логин или пароль"));
        }
    }

    // Можете добавить /register сюда же
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            userDetailsService.registerUser(user);
            return ResponseEntity.ok(Map.of("message", "Регистрация успешна"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // Вложенные классы для DTO
    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class JwtResponse {
        private final String token;

        public JwtResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}
