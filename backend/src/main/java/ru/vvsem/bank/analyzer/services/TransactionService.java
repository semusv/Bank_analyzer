package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.dto.TransactionDto;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    List<TransactionDto> getListTransaction(Long userId);

    TransactionDto getTransaction(Long transactionId, Long userId);

    void hideTransaction(Long transactionId, Long userId);

    void deleteTransaction(Long transactionId, Long userId);

    org.springframework.data.domain.Page<TransactionDto> getListTransaction(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size);
}
