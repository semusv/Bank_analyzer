package ru.vvsem.bank.analyzer.controllers.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import ru.vvsem.bank.analyzer.formatters.ErrorMessageFormatter;


@RequiredArgsConstructor
@ControllerAdvice
@Slf4j
public class WebExceptionHandler {

    private final MessageSource messageSource;

    private final ErrorMessageFormatter errorMessageFormatter;


    // Логирование 500 ошибок
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleAllExceptions(Exception ex, WebRequest request) {

        String errorText = messageSource.getMessage("error.internal.server", null,
                LocaleContextHolder.getLocale());

        logErrorDetails(ex, request, errorText);

        ModelAndView model = new ModelAndView("customError");
        model.addObject("error", errorText);
        model.addObject("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        return model;
    }

    private void logErrorDetails(Exception ex, WebRequest request, String errorText) {
        String formattedLog = errorMessageFormatter.format(ex, request, errorText);
        log.error(formattedLog);
    }

}



