package ru.vvsem.bank.analyzer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;
import java.util.Set;

/**
 * DTO for {@link User}
 */
@Getter
@Setter
public class UserDto {
    private   Long id;

    @NotNull
    private   String login;

    @NotNull
    private   String name;

    @NotNull
    private   String surname;

    private   String patronymic;

    @NotNull
    @Email
    private   String email;

    private   List<BankAccountDto> bankAccounts;

    private   Set<CategoryDto> categories;

}