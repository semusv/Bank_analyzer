package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.math.BigDecimal;

/**
 * DTO for {@link Transaction}
 */
@Getter
@Setter
public class SubTransactionDto {

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal amount;
}