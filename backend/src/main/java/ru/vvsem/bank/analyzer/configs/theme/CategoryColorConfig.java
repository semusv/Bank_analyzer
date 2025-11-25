package ru.vvsem.bank.analyzer.configs.theme;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "colors.category")
@Getter
@Setter
public class CategoryColorConfig {
    private Map<String, String> backgroundColors;

    private Map<String, String> textColors;

    public List<String> getBackgroundColorsList() {
        return backgroundColors != null ?
                new ArrayList<>(backgroundColors.values()) :
                Collections.emptyList();
    }

    public List<String> getTextColorsList() {
        return textColors != null ?
                new ArrayList<>(textColors.values()) :
                Collections.emptyList();
    }

    public String getBackgroundColor(String key) {
        return backgroundColors != null ? backgroundColors.get(key) : null;
    }

    public String getTextColor(String key) {
        return textColors != null ? textColors.get(key) : null;
    }
}
