package ru.vvsem.bank.analyzer.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.BankAccount;


import java.math.BigDecimal;

/**
 * DTO for {@link BankAccount}
 */
@Getter
@Setter
public class NewBankAccountDto {

    @NotBlank(message =
            "{validation.BankAccount.accountName.notBlank}")
    @Size(max = 50, message =
            "{validation.BankAccount.accountName.size}")
    private String name;

    @NotBlank(message =
            "{validation.BankAccount.accountNumber.notBlank}")
    @Size(min = 20, max = 20, message =
            "{validation.BankAccount.accountNumber.size}")
    private String accountNumber;

    @NotNull(message =
            "{validation.BankAccount.bankId.notNull}")
    private Long bankId;

    @NotNull(message = "{validation.BankAccount.currencyId.notNull}")
    private Long currencyId;

    @NotNull
    private BigDecimal initialBalance;
}
