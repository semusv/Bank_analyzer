package ru.vvsem.bank.analyzer.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JwtResponseDto {

    private String token;
}
