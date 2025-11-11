package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Transaction}
 */
@Getter
@Setter
public class TransactionDtoWithSiblings {
    private Long id;

    @NotNull
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDateTime operationTime;

    private boolean hide;

    private boolean master;

    @NotNull
    private Long userId;

    @NotNull
    private OperationType operationType;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long cardId;

    @NotNull
    private Long cardAccountId;

    @NotNull
    private String bankCode;

    @NotNull
    private String bankId;

    @NotNull
    @NotEmpty
    private String currencyCode;

    private Long parentTransactionId;

    private List<TransactionDtoWithSiblings> subTransactions = new ArrayList<>();
}