package ru.vvsem.bank.analyzer.services.transaction;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.time.LocalDateTime;

public interface TransactionSearchService {
    @SuppressWarnings("checkstyle:ParameterNumber")
    @Transactional(readOnly = true)
    Page<TransactionDto> searchTransactions(
            SecurityUser securityUser,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size);
}
