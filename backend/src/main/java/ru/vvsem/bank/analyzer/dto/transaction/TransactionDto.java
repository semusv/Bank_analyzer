package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.dto.CategoryDto;
import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.dto.card.CardDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for {@link ru.vvsem.bank.analyzer.models.Transaction}
 */
@Getter
@Setter
public class TransactionDto {
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
    private CurrencyDto currency;

    private CategoryDto category;

    private CardDto card;

    private BankDto bank;

    private Long userId;
}