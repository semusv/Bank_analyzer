package ru.vvsem.bank.analyzer.dto.validator;

import jakarta.annotation.Nullable;

public record ValidationError(
        @Nullable String field,
        String message,
        @Nullable Object rejectedValue
) {
}
