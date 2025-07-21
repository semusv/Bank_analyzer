package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.UserDto;
import ru.vvsem.bank.analyzer.mappers.UserMapper;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

@RequiredArgsConstructor
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    public UserDto getUserByLogin(String login) {
        return userRepository.findByLogin(login).map(userMapper::toUserDto).orElseThrow();
    }

}
