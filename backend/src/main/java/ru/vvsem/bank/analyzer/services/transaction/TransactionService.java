package ru.vvsem.bank.analyzer.services.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;


import java.util.List;

public interface TransactionService {

    List<TransactionDto> getUserTransactionDtoList(Long userId);

    TransactionDto getTransactionDtoByIdAndUserId(Long transactionId, Long userId);

    void deleteTransaction(Long transactionId, SecurityUser securityUser);

    Page<TransactionDtoWithSiblings> searchTransactions(Specification<Transaction> criteria, Pageable pageable);

    TransactionDto hideTransaction(Long transactionId, Long userId);

    TransactionDto updateTransaction(Transaction transaction, Long userId);

    TransactionDto createTransaction(Transaction transaction);

    List<TransactionDto> getByParentTransactionId(Long parentTransactionId);
}