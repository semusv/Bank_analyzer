package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link Transaction}
 */
@Getter
@Setter
public class NewTransactionDto {
    @NotNull
    @NotBlank(message = "{validation.Transaction.description.notBlank}")
    private String description;

    @NotNull(message = "{validation.Transaction.amount.NotNull}")
    private BigDecimal amount;

    @NotNull(message = "{validation.Transaction.operationTime.NotNull}")
    private LocalDateTime operationTime;

    @NotNull(message = "{validation.Transaction.categoryId.NotNull}")
    private Long categoryId;

    @NotNull(message = "{validation.Transaction.cardId.NotNull}")
    private Long cardId;

    private Long revCardId;

    @NotNull(message = "{validation.Transaction.operationType.NotNull}")
    private OperationType operationType;
}