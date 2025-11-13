package ru.vvsem.bank.analyzer.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.User;

/**
 * DTO for {@link User}
 */
@NoArgsConstructor
@Getter
@Setter
public class RegisterFormDto {
    @NotBlank(message = "{validation.registerForm.name.NotBlank}")
    private String name;

    @NotBlank(message = "{validation.registerForm.login.NotBlank}")
    private String login;

    @NotBlank(message = "{validation.registerForm.surname.NotBlank}")
    private String surname;

    private String patronymic;

    @Email(message = "{validation.registerForm.email.Email}")
    @NotBlank(message = "{validation.registerForm.email.NotBlank}")
    private String email;

    @NotBlank(message = "{validation.registerForm.password.NotBlank}")
    @Pattern(message = "{validation.registerForm.password.regexp}",
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    private String password;
}