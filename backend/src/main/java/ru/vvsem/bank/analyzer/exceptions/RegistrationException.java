package ru.vvsem.bank.analyzer.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class RegistrationException  extends CustomExceptionWithCode {


  public RegistrationException(String messageCode, Object[] messageArgs) {
    super(messageCode, messageArgs);
  }

  public RegistrationException(String message, String messageCode, Object... messageArgs) {
    super(message, messageCode, messageArgs);
  }
}