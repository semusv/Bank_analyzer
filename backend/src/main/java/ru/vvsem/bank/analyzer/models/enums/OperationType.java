package ru.vvsem.bank.analyzer.models.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    CARD("Банковская карта"),
    CASH("Наличные"),
    TRANSFER("Перевод между счетами"),
    OTHER("Прочее");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

}