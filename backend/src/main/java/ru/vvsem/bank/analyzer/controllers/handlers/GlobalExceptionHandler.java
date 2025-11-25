package ru.vvsem.bank.analyzer.controllers.handlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.vvsem.bank.analyzer.exceptions.BusinessException;
import ru.vvsem.bank.analyzer.exceptions.CustomExceptionWithCode;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.exceptions.RegistrationException;
import ru.vvsem.bank.analyzer.providers.ErrorHandlingProvider;

import java.util.concurrent.TimeoutException;

@RestControllerAdvice(basePackages = "ru.vvsem.bank.analyzer.controllers.api")
@AllArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorHandlingProvider errorHandlingProvider;

    private final RequestToViewNameTranslator requestToViewNameTranslator;

    // 400 - Method argument type mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        var requiredType = ex.getRequiredType();
        String requiredTypeText = requiredType != null ? requiredType.getSimpleName() : "unknown";
        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.BAD_REQUEST,
                "error.argument.type.mismatch",
                ex.getName(), ex.getValue(), requiredTypeText);
    }

    //401 AuthenticationException
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.UNAUTHORIZED,
                "error.authentication.failed");

    }

    //403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.FORBIDDEN,
                "error.access.denied");
    }

    //404
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundForWeb(
            EntityNotFoundException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                ex.getMessageCode(),
                ex.getMessageArgs());

    }

    // 404 - Not Found
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            WebRequest request) {
        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                "error.no.handler.found",
                ((ServletWebRequest) request).getRequest().getRequestURI());

    }

    // 404 - Empty result data access
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccess(
            EmptyResultDataAccessException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                "error.data.not.found");
    }


    // 500 - Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "error.internal.server");
    }


    // 503 - Database access errors
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(
            DataAccessException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.SERVICE_UNAVAILABLE,
                "error.database.access",
                (Object[]) null);
    }

    // 408 - Timeout
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<Object> handleTimeoutException(
            TimeoutException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.REQUEST_TIMEOUT,
                "error.request.timeout",
                (Object[]) null);
    }

    //BusinessException
    //RegistrationException
    @ExceptionHandler(
            value = {
                    BusinessException.class,
                    RegistrationException.class
            })
    public ResponseEntity<Object> handleBusinessException(
            CustomExceptionWithCode ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.CONFLICT,
                ex.getMessageCode(),
                ex.getMessageArgs());

    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Object> handleTransactionSystemException(
            TransactionSystemException ex,
            WebRequest request) {

        Throwable cause = ex.getRootCause();
        if (cause instanceof ConstraintViolationException constraintEx) {
            return handleConstraintViolation(constraintEx, request);
        }

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "error.transaction.failed",
                ex.getMostSpecificCause().getMessage());
    }


    private ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex,
            WebRequest request) {

        String firstError = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation failed");

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.BAD_REQUEST,
                firstError);
    }

}