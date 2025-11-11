package ru.vvsem.bank.analyzer.providers;

import org.springframework.security.access.AccessDeniedException;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.EntityName;

public interface EntityAccessProvider {
    Category getOwnedCategory(Long categoryId, Long userId);

    Card getOwnedCard(Long cardId, Long userId);

    Transaction getOwnedTransaction(Long transactionId, Long userId);

    BankAccount getOwnedBankAccount(Long bankAccountId, Long userId);

    AccessDeniedException throwEntityAccessDenied(EntityName entityName, Long categoryId, Long userId);

    EntityNotFoundException throwEntityNotFound(EntityName entityName, Long id, Long userId);

    EntityNotFoundException throwEntityNotFound(EntityName entityName, Long id);
}
