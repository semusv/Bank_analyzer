package ru.vvsem.bank.analyzer.formatters;

import org.springframework.web.context.request.WebRequest;

public interface ErrorMessageFormatter {
    String format(Exception ex, WebRequest request, String errorText);
}
