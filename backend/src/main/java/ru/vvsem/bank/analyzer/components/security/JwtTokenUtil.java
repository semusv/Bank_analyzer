package ru.vvsem.bank.analyzer.components.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtTokenUtil {
    String generateToken(UserDetails userDetails);

    Boolean validateToken(String token, UserDetails userDetails);

    String extractUsername(String token);

    Date extractExpiration(String token);
}
