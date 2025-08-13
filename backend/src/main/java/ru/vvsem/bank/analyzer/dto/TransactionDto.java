package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Transaction}
 */
@Getter
@Setter
public class TransactionDto {
    private  Long id;

    @NotNull
    private  String description;

    @NotNull
    private  BigDecimal amount;

    @NotNull
    private  LocalDateTime operationTime;

    private  boolean hide;

    private   boolean master;

    @NotNull
    private  CurrencyDto currency;

    private  CategoryDto category;

    private  CardDto card;

    @NotNull
    private  OperationType operationType;

    private  Long userId;

    private  TransactionDto parentTransaction;

    private  List<TransactionDto> subTransactions;
}