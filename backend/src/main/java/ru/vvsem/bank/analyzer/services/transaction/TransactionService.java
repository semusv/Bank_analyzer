package ru.vvsem.bank.analyzer.services.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;


import java.util.List;

public interface TransactionService {

    @Transactional(readOnly = true)
    List<TransactionDto> getUserTransactionDtoList(Long userId);

    @Transactional(readOnly = true)
    TransactionDto getTransactionDtoByIdAndUserId(Long transactionId, Long userId);

    @Transactional
    void deleteTransaction(Long transactionId, SecurityUser securityUser);

    @Transactional
    Page<TransactionDtoWithSiblings> searchTransactions(Specification<Transaction> criteria, Pageable pageable);

    @Transactional
    TransactionDto hideTransaction(Long transactionId, Long userId);

    @Transactional
    TransactionDto updateTransaction(Transaction transaction, Long userId);

    @Transactional
    TransactionDto createTransaction(Transaction transaction);

    @Transactional(readOnly = true)
    List<TransactionDto> getByParentTransactionId(Long parentTransactionId);
}