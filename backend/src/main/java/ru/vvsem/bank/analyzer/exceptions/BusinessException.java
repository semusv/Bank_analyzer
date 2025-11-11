package ru.vvsem.bank.analyzer.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BusinessException extends RuntimeException {

  private final String messageCode;

  private final transient Object[] messageArgs;

  public BusinessException(String message, String messageCode, Object... messageArgs) {
    super(message);
    this.messageCode = messageCode;
    this.messageArgs = messageArgs;
  }
}