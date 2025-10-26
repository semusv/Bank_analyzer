package ru.vvsem.bank.analyzer.exceptions;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String messageCode;

    private final transient Object[] messageArgs;

    public EntityNotFoundException(String message, String messageCode, Object... messageArgs) {
        super(message);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs;
    }
}
