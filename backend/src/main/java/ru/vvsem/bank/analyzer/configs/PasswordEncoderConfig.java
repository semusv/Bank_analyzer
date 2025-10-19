package ru.vvsem.bank.analyzer.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.RequestToViewNameTranslator;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder(RequestToViewNameTranslator requestToViewNameTranslator) {
        return new BCryptPasswordEncoder();
    }
}
