package ru.vvsem.bank.analyzer.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.EntityName;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.BankRepository;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;
import ru.vvsem.bank.analyzer.repositories.CurrencyRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

@Component
@RequiredArgsConstructor
public class EntityAccessProviderImpl implements EntityAccessProvider {

    private final CategoryRepository categoryRepository;

    private final CardRepository cardRepository;

    private final TransactionRepository transactionRepository;

    private final BankRepository bankRepository;

    private final BankAccountRepository bankAccountRepository;

    private final CurrencyRepository currencyRepository;

    @Override
    public Category requireOwnedCategory(Long categoryId, Long userId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.CATEGORY, categoryId, userId));
    }

    @Override
    public Card requireOwnedCard(Long cardId, Long userId) {
        return cardRepository.findByIdAndAccountUserId(cardId, userId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.CARD, cardId, userId));
    }

    @Override
    public Transaction requireOwnedTransaction(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.TRANSACTION, transactionId, userId));
    }

    @Override
    public Currency requireCurrency(Long currencyId) {
        return currencyRepository.findById(currencyId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.CURRENCY, currencyId));
    }

    @Override
    public Currency requireCurrencyByCardId(Long cardId) {
        return currencyRepository.getCurrencyByCardId(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(cardId),
                        "exception.entity.not.found.currency"));
    }

    @Override
    public Bank requireBank(Long bankId) {
        return bankRepository.findById(bankId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.BANK, bankId));
    }

    @Override
    public BankAccount requireOwnedBankAccount(Long bankAccountId, Long userId) {
        return bankAccountRepository.findByIdAndUserId(bankAccountId, userId)
                .orElseThrow(() ->
                        entityNotFound(EntityName.CATEGORY, bankAccountId, userId));
    }


    private EntityNotFoundException entityNotFound(EntityName entityName, Long id, Long userId) {
        return new EntityNotFoundException(
                "%s for id %d and userId %d not found".formatted(entityName.getDescription(), id, userId),
                "exception.entity.not.found.entity",
                entityName.getDescription());
    }


    private EntityNotFoundException entityNotFound(EntityName entityName, Long id) {
        return new EntityNotFoundException(
                "%s for id %d not found".formatted(entityName.getDescription(), id),
                "exception.entity.not.found.entity",
                entityName.getDescription());
    }

}
