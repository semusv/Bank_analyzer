package ru.vvsem.bank.analyzer.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.templates")
@Getter
@Setter
public class ErrorTemplateConfig {
    private String error;
}
