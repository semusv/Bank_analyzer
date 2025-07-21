package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;
import java.util.Set;

/**
 * DTO for {@link User}
 */
@Value
public class UserDto {
    Long id;

    @NotNull
    String login;

    @NotNull
    String name;

    @NotNull
    String surname;

    String patronymic;

    @NotNull
    @Email
    String email;

    List<BankAccountDto> bankAccounts;

    Set<CategoryDto> categories;

    Set<BudgetDto> budgets;

}