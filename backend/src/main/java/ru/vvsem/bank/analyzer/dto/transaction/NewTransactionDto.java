package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotBlank(message = "{validation.Transaction.description.notBlank}")
    String description;

    @NotNull(message = "{validation.Transaction.amount.NotNull}")
    BigDecimal amount;

    @NotNull(message = "{validation.Transaction.operationTime.NotNull}")
    LocalDateTime operationTime;

    @NotNull(message = "{validation.Transaction.categoryId.NotNull}")
    Long categoryId;

    @NotNull(message = "{validation.Transaction.cardId.NotNull}")
    Long cardId;

    @NotNull(message = "{validation.Transaction.operationType.NotNull}")
    OperationType operationType;
}