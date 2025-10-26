package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

public interface BankAccountService {
    @Transactional(readOnly = true)
    List<BankAccountDto> getUserBankAccount(Long userId);

    @Transactional
    BankAccountSimpleDto createAccount(NewBankAccountDto newBankAccountDto, User use);

    @Transactional
    List<BankAccountSimpleDto> getUserAccountsWithCards(Long userId);

    @Transactional
    void deleteAccount(Long accountId, Long userId);
}
