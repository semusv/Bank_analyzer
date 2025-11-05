package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;


import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    @Transactional(readOnly = true)
    List<TransactionDto> getListTransaction(SecurityUser securityUser);

    @Transactional(readOnly = true)
    TransactionDto getTransaction(Long transactionId, SecurityUser securityUser);

    @Transactional
    void hideTransaction(Long transactionId, SecurityUser securityUser);

    @Transactional
    void deleteTransaction(Long transactionId, SecurityUser securityUser);

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional(readOnly = true)
    Page<TransactionDto> getListTransaction(
            SecurityUser securityUser,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size);

    @Transactional
    void splitTransaction(Long transactionId, @Valid List<SubTransactionDto> subTransactions, SecurityUser securityUser);

    @Transactional
    List<TransactionDto>  insertTransaction(@Valid NewTransactionDto newTransactionDto, SecurityUser securityUser);
}
