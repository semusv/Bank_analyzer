package ru.vvsem.bank.analyzer.services;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.vvsem.bank.analyzer.formatters.ErrorMessageFormatter;


import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorHandlingServiceImpl implements ErrorHandlingService {

    private final ErrorMessageFormatter errorMessageFormatter;

    private final MessageSource messageSource;

    @Override
    public ResponseEntity<Object> handleError(
            Exception ex,
            WebRequest request,
            HttpStatus status,
            String messageCode,
            Object... args) {

        String errorText = getLocalizedErrorMessage(messageCode, args);
        logErrorDetails(ex, request, errorText);
        return buildApiErrorResponse(ex, errorText, status, getRequestPath(request));
    }


    private String getLocalizedErrorMessage(String messageCode, Object... args) {
        return messageSource.getMessage(
                messageCode,
                args,
                "Server Error",
                LocaleContextHolder.getLocale()
        );
    }

    private void logErrorDetails(Exception ex, WebRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }

    private ResponseEntity<Object> buildApiErrorResponse(
            Exception ex,
            String errorText,
            HttpStatus status,
            String requestPath) {

        Map<String, Object> body = buildErrorResponseBody(ex, errorText, status, requestPath);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private Map<String, Object> buildErrorResponseBody(
            Exception ex,
            String errorText,
            HttpStatus status,
            String requestPath) {

        return Map.of(
                "errorText", errorText,
                "status", status.value(),
                "timestamp", LocalDateTime.now(),
                "path", requestPath,
                "exception", ex.getClass().getSimpleName()
        );
    }

    private String getRequestPath(@Nullable WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "unknown";
    }
}
