package ru.vvsem.bank.analyzer.services;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.User;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    @Transactional(readOnly = true)
    List<TransactionDto> getListTransaction(Long userId);

    @Transactional(readOnly = true)
    TransactionDto getTransaction(Long transactionId, Long userId);

    @Transactional
    void hideTransaction(Long transactionId, Long userId);

    @Transactional
    void deleteTransaction(Long transactionId, Long userId);

    @SuppressWarnings("CheckStyle")
    @Transactional(readOnly = true)
    Page<TransactionDto> getListTransaction(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size);

    @Transactional
    void splitTransaction(Long transactionId, @Valid List<SubTransactionDto> subTransactions, Long userId);

    @Transactional
    TransactionDto insertTransaction(@Valid NewTransactionDto newTransactionDto, User user);
}
