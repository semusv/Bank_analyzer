package ru.vvsem.bank.analyzer.services.security;

public interface PasswordService {
    String encodePassword(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
