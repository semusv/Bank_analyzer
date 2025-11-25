package ru.vvsem.bank.analyzer.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomExceptionWithCode extends RuntimeException {

  private final String messageCode;

  private final transient Object[] messageArgs;

  public CustomExceptionWithCode(String message, String messageCode, Object... messageArgs) {
    super(message);
    this.messageCode = messageCode;
    this.messageArgs = messageArgs;
  }
}