package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.dto.UserDto;

public interface UserService {
    UserDto getUserByLogin(String login);
}
