package ru.vvsem.bank.analyzer.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

public interface  ErrorHandlingService {

    ResponseEntity<Object> handleError(
            Exception ex,
            WebRequest request,
            HttpStatus status,
            String messageCode,
            Object... args);


}
