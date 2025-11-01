package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.User;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {
    @Transactional(readOnly = true)
    List<BankAccountDto> getUserBankAccount(Long userId);

    @Transactional
    BankAccountSimpleDto createAccount(NewBankAccountDto newBankAccountDto, User user);

    @Transactional
    List<BankAccountSimpleDto> getUserAccountsWithCards(User user);

    @Transactional
    void deleteAccount(Long accountId, User user);

    void updateAccountBalance(BankAccount bankAccount, BigDecimal amount);
}
