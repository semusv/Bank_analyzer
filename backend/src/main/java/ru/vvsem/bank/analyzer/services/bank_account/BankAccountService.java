package ru.vvsem.bank.analyzer.services.bank_account;

import ru.vvsem.bank.analyzer.dto.account.BankAccountWithCardsDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.math.BigDecimal;
import java.util.List;

public interface BankAccountService {

    BankAccountWithCardsDto createAccount(NewBankAccountDto newBankAccountDto, SecurityUser securityUser);

    List<BankAccountWithCardsDto> getUserAccountsWithCards(SecurityUser securityUser);

    void deleteAccount(Long accountId, SecurityUser securityUser);

    void addAccountBalance(Long accountId, BigDecimal amount, SecurityUser securityUser);
}
