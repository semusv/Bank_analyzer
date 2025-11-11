package ru.vvsem.bank.analyzer.providers;

import org.springframework.security.access.AccessDeniedException;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.EntityName;

public interface EntityAccessProvider {
    Category requireOwnedCategory(Long categoryId, Long userId);

    Card requireOwnedCard(Long cardId, Long userId);

    Transaction requireOwnedTransaction(Long transactionId, Long userId);

    BankAccount requireOwnedBankAccount(Long bankAccountId, Long userId);

    AccessDeniedException entityAccessDenied(EntityName entityName, Long categoryId, Long userId);

    EntityNotFoundException entityNotFound(EntityName entityName, Long id, Long userId);

    EntityNotFoundException entityNotFound(EntityName entityName, Long id);
}
