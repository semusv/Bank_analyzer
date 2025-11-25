package ru.vvsem.bank.analyzer.dto.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.math.BigDecimal;

/**
 * DTO for {@link Transaction}
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubTransactionDto {

    @NotBlank
    private String description;

    @NotNull
    private BigDecimal amount;
}