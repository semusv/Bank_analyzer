package ru.vvsem.bank.analyzer.services;

public interface PasswordService {
    String encodePassword(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
