package ru.vvsem.bank.analyzer.models.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    OUTGOING("Расход"),
    INCOMING("Доход"),
    TRANSFER("Перевод");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

}