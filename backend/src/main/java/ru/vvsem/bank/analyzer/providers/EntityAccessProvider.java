package ru.vvsem.bank.analyzer.providers;

import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.Transaction;

public interface EntityAccessProvider {
    Category requireOwnedCategory(Long categoryId, Long userId);

    Card requireOwnedCard(Long cardId, Long userId);

    Transaction requireOwnedTransaction(Long transactionId, Long userId);

    Currency requireCurrency(Long currencyId);

    Currency requireCurrencyByCardId(Long cardId);

    Bank requireBank(Long bankId);

    BankAccount requireOwnedBankAccount(Long bankAccountId, Long userId);
}
