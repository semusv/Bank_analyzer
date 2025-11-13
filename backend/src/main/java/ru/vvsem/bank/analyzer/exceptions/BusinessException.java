package ru.vvsem.bank.analyzer.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends CustomExceptionWithCode {

  public BusinessException(String messageCode, Object[] messageArgs) {
    super(messageCode, messageArgs);
  }

  public BusinessException(String message, String messageCode, Object... messageArgs) {
    super(message, messageCode, messageArgs);
  }
}