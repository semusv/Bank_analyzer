package ru.vvsem.bank.analyzer.models.enums;

import lombok.Getter;

@Getter
public enum EntityName {
    CATEGORY("category"),
    CARD("card"),
    TRANSACTION("transaction"),
    USER("user"),
    CURRENCY("currency"),
    BANK("bank");

    private final String description;

    EntityName(String description) {
        this.description = description;
    }
}
