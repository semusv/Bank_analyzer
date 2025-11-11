package ru.vvsem.bank.analyzer.providers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
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
    public Category getOwnedCategory(Long categoryId, Long userId) {
        if (categoryId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Category id and user id must not be null");
        }
        var entity = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        throwEntityNotFound(EntityName.CATEGORY, categoryId, userId));
        if (!entity.getUser().getId().equals(userId)) {
            throw throwEntityAccessDenied(EntityName.CATEGORY, entity.getId(), userId);
        }
        return entity;
    }


    @Override
    public Card getOwnedCard(Long cardId, Long userId) {
        if (cardId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Card id and user id must not be null");
        }
        var entity = cardRepository.findById(cardId)
                .orElseThrow(() ->
                        throwEntityNotFound(EntityName.CARD, cardId, userId));
        if (!entity.getAccount().getUser().getId().equals(userId)) {
            throw throwEntityAccessDenied(EntityName.CARD, entity.getId(), userId);
        }
        return entity;
    }

    @Override
    public Transaction getOwnedTransaction(Long transactionId, Long userId) {
        if (transactionId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Transaction id and user id must not be null");
        }
        var entity = transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        throwEntityNotFound(EntityName.TRANSACTION, transactionId, userId));
        if (!entity.getUser().getId().equals(userId)) {
            throw throwEntityAccessDenied(EntityName.TRANSACTION, entity.getId(), userId);
        }
        return entity;
    }

    @Override
    public BankAccount getOwnedBankAccount(Long bankAccountId, Long userId) {
        if (bankAccountId == null || userId == null) {
            throw new IllegalArgumentException(
                    "Bank account id and user id must not be null");
        }
        var entity = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() ->
                        throwEntityNotFound(EntityName.CATEGORY, bankAccountId, userId));
        if (!entity.getUser().getId().equals(userId)) {
            throw throwEntityAccessDenied(EntityName.TRANSACTION, entity.getId(), userId);
        }
        return entity;

    }

    @Override
    public AccessDeniedException throwEntityAccessDenied(EntityName entityName, Long categoryId, Long userId) {
        return new AccessDeniedException(
                "%s for id %d and userId %d denied".formatted(entityName.getDescription(), categoryId, userId));
    }

    @Override
    public EntityNotFoundException throwEntityNotFound(EntityName entityName, Long id, Long userId) {
        return new EntityNotFoundException(
                "%s for id %d and userId %d not found".formatted(entityName.getDescription(), id, userId),
                "exception.entity.not.found.entity",
                entityName.getDescription());
    }

    @Override
    public EntityNotFoundException throwEntityNotFound(EntityName entityName, Long id) {
        return new EntityNotFoundException(
                "%s for id %d not found".formatted(entityName.getDescription(), id),
                "exception.entity.not.found.entity",
                entityName.getDescription());
    }
}
