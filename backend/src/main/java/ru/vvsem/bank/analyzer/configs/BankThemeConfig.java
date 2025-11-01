package ru.vvsem.bank.analyzer.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.vvsem.bank.analyzer.models.BankTheme;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "colors.bank")
@Getter
@Setter
public class BankThemeConfig {
    private Map<String, BankTheme> themes = new HashMap<>();



    public BankTheme getThemeForBank(String bankCode) {
        return themes.getOrDefault(bankCode, getDefaultTheme());
    }

    private BankTheme getDefaultTheme() {
        return themes.get("default");
    }
}
