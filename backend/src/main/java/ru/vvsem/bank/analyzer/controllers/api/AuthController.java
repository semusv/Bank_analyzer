package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.auth.LoginRequestDto;
import ru.vvsem.bank.analyzer.dto.auth.RegisterFormDto;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;
import ru.vvsem.bank.analyzer.utils.security.JwtTokenUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String JWT_TOKEN = "JWT_TOKEN";

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService userDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.cookie.expiration.seconds}")
    private Long expiration;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public Map<?, ?> login(
            @Valid  @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        Cookie cookie = new Cookie(JWT_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);  // Только HTTP
        cookie.setPath("/");
        cookie.setMaxAge(Math.toIntExact(expiration)); //
        response.addCookie(cookie);

        return Map.of("token", token);
    }


    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        // Удаляем JWT_TOKEN куку
        Cookie jwtCookie = new Cookie(JWT_TOKEN, null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public void register(@Valid @RequestBody RegisterFormDto registerDto) {
        userDetailsService.registerUser(registerDto);
    }
}
