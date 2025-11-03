package ru.vvsem.bank.analyzer.services.transaction;

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
    List<TransactionDto> getListTransaction(User user);

    @Transactional(readOnly = true)
    TransactionDto getTransaction(Long transactionId, User user);

    @Transactional
    void hideTransaction(Long transactionId, User user);

    @Transactional
    void deleteTransaction(Long transactionId, User user);

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional(readOnly = true)
    Page<TransactionDto> getListTransaction(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size);

    @Transactional
    void splitTransaction(Long transactionId, @Valid List<SubTransactionDto> subTransactions, User user);

    @Transactional
    TransactionDto insertTransaction(@Valid NewTransactionDto newTransactionDto, User user);
}
