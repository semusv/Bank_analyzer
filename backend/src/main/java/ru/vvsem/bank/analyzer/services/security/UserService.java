package ru.vvsem.bank.analyzer.services.security;

import ru.vvsem.bank.analyzer.dto.UserDto;

public interface UserService {
    UserDto getUserByLogin(String login);

    UserDto getCurrentUser();

    String getCurrentUsername();

}
