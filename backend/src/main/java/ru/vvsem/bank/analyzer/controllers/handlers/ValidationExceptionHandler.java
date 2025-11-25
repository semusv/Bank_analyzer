package ru.vvsem.bank.analyzer.controllers.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.vvsem.bank.analyzer.dto.validator.ValidationError;
import ru.vvsem.bank.analyzer.dto.validator.ValidationErrorResponse;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationExceptionHandler {

    private final MessageSource messageSource;

    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            @Nullable WebRequest request) {

        List<ValidationError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::mapToValidationError)
                .toList();

        ValidationErrorResponse response = new ValidationErrorResponse(
                "VALIDATION_FAILED",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                getRequestPath(request),
                errors
        );

        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    private ValidationError mapToValidationError(ObjectError error) {
        if (error instanceof FieldError fieldError) {
            return new ValidationError(
                    fieldError.getField(),
                    getLocalizedMessage(fieldError),
                    fieldError.getRejectedValue()
            );
        }
        return new ValidationError(
                null,
                getLocalizedMessage(error),
                null
        );
    }

    private String getLocalizedMessage(ObjectError error) {

        if (error == null || error.getCode() == null) {
            return "Validate Error";
        }

        String defaultMessage = error.getDefaultMessage();
        if (defaultMessage == null) {
            defaultMessage = "Validate Error";
        }

        return messageSource.getMessage(
                error.getCode(),
                error.getArguments(),
                defaultMessage,
                LocaleContextHolder.getLocale()
        );
    }

    private String getRequestPath(@Nullable WebRequest request) {
        return request != null ?
                ((ServletWebRequest) request).getRequest().getRequestURI() :
                "N/A";
    }
}
