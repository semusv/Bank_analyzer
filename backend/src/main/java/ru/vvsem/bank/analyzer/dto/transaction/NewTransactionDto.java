package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
@Value
public class NewTransactionDto {
    @NotNull
    String description;
    @NotNull
    BigDecimal amount;
    @NotNull
    LocalDateTime operationTime;
    Long categoryId;
    Long cardId;
    @NotNull
    OperationType operationType;
}