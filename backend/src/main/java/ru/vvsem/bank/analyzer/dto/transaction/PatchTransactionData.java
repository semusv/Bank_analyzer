package ru.vvsem.bank.analyzer.dto.transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PatchTransactionData {
    @NotNull(message = "{validation.Transaction.amount.NotNull}")
    private BigDecimal amount;

    @NotNull(message = "{validation.Transaction.operationTime.NotNull}")
    private LocalDateTime operationTime;

    @NotNull(message = "{validation.Transaction.categoryId.NotNull}")
    private Long categoryId;
}
