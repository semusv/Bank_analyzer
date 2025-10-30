package ru.vvsem.bank.analyzer.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "bank")
@Getter
@Setter
public class BankThemeConfig {
    private Map<String, BankTheme> themes = new HashMap<>();

    @Getter
    @Setter
    public static class BankTheme {
        private String primaryColor;
        private String textColor;
        private String secondaryColor;
    }

    public BankTheme getThemeForBank(String bankCode) {
        return themes.getOrDefault(bankCode, getDefaultTheme());
    }

    private BankTheme getDefaultTheme() {
        return themes.get("default");
    }
}
