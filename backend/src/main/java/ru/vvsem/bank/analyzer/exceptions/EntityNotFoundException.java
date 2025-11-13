package ru.vvsem.bank.analyzer.exceptions;

import lombok.Getter;

@Getter
public class EntityNotFoundException  extends CustomExceptionWithCode {


    public EntityNotFoundException(String messageCode, Object[] messageArgs) {
        super(messageCode, messageArgs);
    }

    public EntityNotFoundException(String message, String messageCode, Object... messageArgs) {
        super(message, messageCode, messageArgs);
    }
}
