package ru.vvsem.bank.analyzer.services.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;


import java.util.List;

public interface TransactionService {

    @Transactional(readOnly = true)
    List<TransactionDto> getUserTransactions(Long userId);

    @Transactional(readOnly = true)
    TransactionDto getUserTransaction(Long transactionId, Long userId);

    @Transactional
    void deleteTransaction(Long transactionId, SecurityUser securityUser);

    @Transactional
    public Page<TransactionDto> searchTransactions(Specification<Transaction> criteria, Pageable pageable);

    @Transactional
    public TransactionDto hideTransaction(Long transactionId, Long userId);

    @Transactional
    public TransactionDto updateTransaction(Transaction transaction, Long userId);

    @Transactional
    public TransactionDto createTransaction(Transaction transaction);


}
