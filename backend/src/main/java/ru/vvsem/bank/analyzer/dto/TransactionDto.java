package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Transaction}
 */
@Value
public class TransactionDto {
    Long id;

    @NotNull
    String description;

    @NotNull
    BigDecimal amount;

    @NotNull
    LocalDateTime operationTime;

    boolean hide;

    boolean master;

    @NotNull
    CurrencyDto currency;

    CategoryDto category;

    CardDto card;

    @NotNull
    OperationType operationType;

    Long userId;

    TransactionDto parentTransaction;

    List<TransactionDto> subTransactions;
}