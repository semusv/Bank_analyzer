package ru.vvsem.bank.analyzer.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.vvsem.bank.analyzer.dto.validator.ValidationErrorResponse;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.services.handlers.ErrorHandlingService;


@Slf4j
@RestControllerAdvice(basePackages = "ru.vvsem.bank.analyzer.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ValidationExceptionHandler validationExceptionHandler;

    private final ErrorHandlingService errorHandlingService;

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundForWeb(
            EntityNotFoundException ex,
            WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                ex.getMessageCode(),
                ex.getMessageArgs());

    }

    //отсутствующий обязательный параметр запроса
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @Nullable MissingServletRequestParameterException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        assert status != null;
        assert ex != null;

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.valueOf(status.value()),
                "error.missing.servlet.request.parameter",
                ex.getParameterName());
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @Nullable MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        if (ex == null) {
            return null;
        }

        ResponseEntity<ValidationErrorResponse> responseEntity =
                validationExceptionHandler.handleValidationException(ex, request);

        return ResponseEntity
                .status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .body(responseEntity.getBody());
    }

    //403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.FORBIDDEN,
                "error.access.denied",
                (Object[]) null);
    }


    //500
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @Nullable Exception ex,
            Object body,
            @Nullable HttpHeaders headers,
            HttpStatusCode statusCode,
            @Nullable WebRequest request) {

        return errorHandlingService.handleError(
                ex,
                request,
                HttpStatus.valueOf(statusCode.value()),
                "error.internal.server");
    }
}






