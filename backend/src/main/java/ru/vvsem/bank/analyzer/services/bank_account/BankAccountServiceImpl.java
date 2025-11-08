package ru.vvsem.bank.analyzer.services.bank_account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapper;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
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

    @Transactional(readOnly = true)
    @Override
    public List<BankAccountDto> getUserBankAccount(Long userId) {
        log.info("Getting accounts for user: {}", userId);
        List<BankAccount> accounts = bankAccountRepository.findByUserId(userId);
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountDto).toList();
    }

    @Transactional
    @Override
    public BankAccountSimpleDto createAccount(NewBankAccountDto newBankAccountDto, SecurityUser securityUser) {
        log.info("Creating new account for user: {}", securityUser.getId());

        var bankAccount = bankAccountMapper.toEntity(newBankAccountDto);
        bankAccount.setBank(entityAccessProvider.requireBank(bankAccount.getBank().getId()));
        bankAccount.setCurrency(entityAccessProvider.requireCurrency(bankAccount.getCurrency().getId()));
        bankAccount.setUser(userService.getUserById(securityUser.getId()));

        return bankAccountMapper.toBankAccountSimpleDto(bankAccountRepository.save(bankAccount));
    }

    @Transactional
    @Override
    public List<BankAccountSimpleDto> getUserAccountsWithCards(SecurityUser securityUser) {
        log.info("Getting accounts with cards for user: {}", securityUser.getId());
        List<BankAccount> accounts = bankAccountRepository.findWithCardsAndUserAndCurrencyByUserId(securityUser.getId());
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountSimpleDto).toList();

    }

    @Transactional
    @Override
    public void deleteAccount(Long accountId, SecurityUser securityUser) {
        log.info("Deleting account: {} for user: {}", accountId, securityUser.getId());

        BankAccount account = bankAccountRepository.findByIdAndUserId(accountId, securityUser.getId())
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "AccountId %d for UserId %d not found"
                                                .formatted(accountId, securityUser.getId()),
                                        "exception.entity.not.found.bankAccount")
                );

        bankAccountRepository.delete(account);
    }

    @Override
    public void updateAccountBalance(Long accountId, BigDecimal amount, SecurityUser securityUser) {
        var bankAccount = entityAccessProvider.requireOwnedBankAccount(accountId, securityUser.getId());
        var updatedBankAccount = bankAccountRepository.updateBalance(
                bankAccount.getBalance().add(amount),
                accountId
        );
        if (updatedBankAccount == 0) {
            throw new EntityNotFoundException(
                    "BankAccount with id %d not found".formatted(bankAccount.getId()),
                    "exception.entity.not.found.bankAccount"
            );
        }

    }

}
