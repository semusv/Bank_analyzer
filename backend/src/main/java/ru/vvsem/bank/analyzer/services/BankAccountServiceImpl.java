package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.account.BankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.BankAccountMapper;
import ru.vvsem.bank.analyzer.mappers.UserMapper;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.BankRepository;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.repositories.CurrencyRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final CardRepository cardRepository;

    private final UserService userService;

    private final UserMapper userMapper;

    private final BankAccountMapper bankAccountMapper;

    private final BankRepository bankRepository;

    private final CurrencyRepository currencyRepository;

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
    public BankAccountSimpleDto createAccount(NewBankAccountDto newBankAccountDto, User user) {
        log.info("Creating new account for user: {}", user.getId());

        var bankAccount = bankAccountMapper.toEntity(newBankAccountDto);
        bankAccount.setBank(prepareBank(bankAccount.getBank().getId()));
        bankAccount.setCurrency(prepareCurrency(bankAccount.getCurrency().getId()));
        bankAccount.setUser( user );

        return bankAccountMapper.toBankAccountSimpleDto(bankAccountRepository.save(bankAccount));
    }

    private Bank prepareBank(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "Bank with id %d not found".formatted(id),
                                        "exception.entity.not.found.bank")
                );
    }

    private Currency prepareCurrency(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "Currency with id %d not found".formatted(id),
                                        "exception.entity.not.found.currency")
                );
    }

    @Transactional
    @Override
    public List<BankAccountSimpleDto> getUserAccountsWithCards(Long userId) {
        log.info("Getting accounts with cards for user: {}", userId);
        List<BankAccount> accounts = bankAccountRepository.findWithCardsAndUserAndCurrencyByUserId(userId);
        return accounts.stream()
                .map(bankAccountMapper::toBankAccountSimpleDto).toList();

    }

    @Transactional
    @Override
    public void deleteAccount(Long accountId, Long userId) {
        log.info("Deleting account: {} for user: {}", accountId, userId);

        BankAccount account = bankAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "AccountId %d for UserId %d not found".formatted(accountId,userId),
                                        "exception.entity.not.found.bankAccount")
                );

        bankAccountRepository.delete(account);
    }

}
