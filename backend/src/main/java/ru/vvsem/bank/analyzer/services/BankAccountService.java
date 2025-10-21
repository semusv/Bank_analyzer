package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.BankAccountDto;
import ru.vvsem.bank.analyzer.dto.UserDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapper;
import ru.vvsem.bank.analyzer.mappers.UserMapper;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.CardRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final CardRepository cardRepository;

    private final UserService userService;

    private final UserMapper userMapper;

    private final BankAccountMapper bankAccountMapper;

    @Transactional(readOnly = true)
    public List<BankAccountDto> getUserBankAccount(Long userId) {
        log.info("Getting accounts for user: {}", userId);
        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountDto).toList();
    }

    @Transactional
    public BankAccount createAccount(BankAccount account, Long userId) {
        log.info("Creating new account for user: {}", userId);

        UserDto userDto = userService.getCurrentUser();
        account.setUser(userMapper.toEntity(userDto));

        return bankAccountRepository.save(account);
    }

    @Transactional
    public List<BankAccountSimpleDto> getUserAccountsWithCards(Long userId) {
        log.info("Getting accounts with cards for user: {}", userId);
        List<BankAccount> accounts = bankAccountRepository.findWithCardsAndUserAndCurrencyByUserId(userId);
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountSimpleDto).toList();

    }

    @Transactional
    public void deleteAccount(Long accountId, Long userId) {
        log.info("Deleting account: {} for user: {}", accountId, userId);

        BankAccount account = bankAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or access denied"));

        bankAccountRepository.delete(account);
    }

}
