package ru.vvsem.bank.analyzer.services.bank_account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountWithCardsDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapper;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.services.bank.BankService;
import ru.vvsem.bank.analyzer.services.currency.CurrencyService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final BankAccountMapper bankAccountMapper;

    private final EntityAccessProvider entityAccessProvider;

    private final CustomUserDetailsService userService;

    private final CurrencyService currencyService;

    private final BankService bankService;


    @Transactional
    @Override
    public BankAccountWithCardsDto createAccount(NewBankAccountDto newBankAccountDto, SecurityUser securityUser) {
        var bankAccount = bankAccountMapper.toEntity(newBankAccountDto);
        bankAccount.setBank(bankService.findById(bankAccount.getBank().getId()));
        bankAccount.setCurrency(currencyService.findById(bankAccount.getCurrency().getId()));
        bankAccount.setUser(userService.getUserById(securityUser.getId()));

        return bankAccountMapper.toBankAccountSimpleDto(bankAccountRepository.save(bankAccount));
    }

    @Transactional
    @Override
    public List<BankAccountWithCardsDto> getUserAccountsWithCards(SecurityUser securityUser) {
        List<BankAccount> accounts = bankAccountRepository.findWithCardsAndUserAndCurrencyByUserId(securityUser.getId());
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountSimpleDto).toList();

    }

    @Transactional
    @Override
    public void deleteAccount(Long accountId, SecurityUser securityUser) {
        var bankAccount = entityAccessProvider.getOwnedBankAccount(accountId, securityUser.getId());
        bankAccountRepository.delete(bankAccount);
    }

    @Override
    public void addAccountBalance(Long accountId, BigDecimal amount, SecurityUser securityUser) {
        var bankAccount = entityAccessProvider.getOwnedBankAccount(accountId, securityUser.getId());
        bankAccountRepository.updateBalance(
                bankAccount.getBalance().add(amount),
                accountId
        );
    }

}
