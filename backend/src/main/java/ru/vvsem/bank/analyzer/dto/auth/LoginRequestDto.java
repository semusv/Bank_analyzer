package ru.vvsem.bank.analyzer.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "{validation.loginrequest.username.notBlank}")
    private String username;

    @NotBlank(message = "{validation.loginrequest.password.notBlank}")
    private String password;

}
