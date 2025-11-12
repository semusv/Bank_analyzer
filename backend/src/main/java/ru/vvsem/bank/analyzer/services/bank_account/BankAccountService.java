package ru.vvsem.bank.analyzer.services.bank_account;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountWithCardsDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    @Transactional
    BankAccountWithCardsDto createAccount(NewBankAccountDto newBankAccountDto, SecurityUser securityUser);

    @Transactional(readOnly = true)
    List<BankAccountWithCardsDto> getUserAccountsWithCards(SecurityUser securityUser);

    @Transactional
    void deleteAccount(Long accountId, SecurityUser securityUser);

    @Transactional
    void addAccountBalance(Long accountId, BigDecimal amount, SecurityUser securityUser);
}
