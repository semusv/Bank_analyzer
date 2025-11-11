package ru.vvsem.bank.analyzer.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.vvsem.bank.analyzer.dto.validator.ValidationErrorResponse;
import ru.vvsem.bank.analyzer.providers.ErrorHandlingProvider;


@Slf4j
@RestControllerAdvice(basePackages = "ru.vvsem.bank.analyzer.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private final ValidationExceptionHandler validationExceptionHandler;

    private final ErrorHandlingProvider errorHandlingProvider;

    // 400 - Invalid JSON format
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @Nullable HttpMessageNotReadableException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.BAD_REQUEST,
                "error.invalid.json.format",
                ex != null ? ex.getMostSpecificCause().getMessage() : "Invalid JSON");
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.BAD_REQUEST,
                "error.argument.type.mismatch",
                ex.getPropertyName(), ex.getValue(), ex.getRequiredType());
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.valueOf(status.value()),
                "error.method.validation");
    }

    // 404 - No handler found
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            @Nullable NoHandlerFoundException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.NOT_FOUND,
                "error.no.handler.found",
                ex != null ? ex.getRequestURL() : "unknown");
    }

    // 405 - Method not supported
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            @Nullable HttpRequestMethodNotSupportedException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.METHOD_NOT_ALLOWED,
                "error.method.not.supported",
                ex != null ? ex.getMethod() : "unknown");
    }

    // 409 - Data integrity violation (unique constraints, foreign keys)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.CONFLICT,
                "error.data.integrity.violation",
                ex.getMostSpecificCause().getMessage());
    }

    // 415 - Unsupported media type
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            @Nullable HttpMediaTypeNotSupportedException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "error.unsupported.media.type",
                ex != null ? ex.getContentType() : "unknown");
    }


    // 400 - Missing servlet request parameter
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @Nullable MissingServletRequestParameterException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {

        assert status != null;
        assert ex != null;

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.valueOf(status.value()),
                "error.missing.servlet.request.parameter",
                ex.getParameterName());
    }

    // 400 - Method argument not valid
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

    //500
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            @Nullable Exception ex,
            Object body,
            @Nullable HttpHeaders headers,
            HttpStatusCode statusCode,
            @Nullable WebRequest request) {

        return errorHandlingProvider.handleError(
                ex,
                request,
                HttpStatus.valueOf(statusCode.value()),
                "error.internal.server");
    }


}






