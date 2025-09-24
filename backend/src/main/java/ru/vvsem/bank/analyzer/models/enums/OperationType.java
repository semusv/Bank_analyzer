package ru.vvsem.bank.analyzer.models.enums;

import lombok.Getter;

@Getter
public enum OperationType {
    DEPOSIT("Внесение средств на счет"),
    EXPENSE("Трата средств"),
    INCOME("Получение средств"),
    OTHER("Прочее"),
    TRANSFER("Перевод между счетами"),
    WITHDRAWAL("Cнятие наличных");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

}