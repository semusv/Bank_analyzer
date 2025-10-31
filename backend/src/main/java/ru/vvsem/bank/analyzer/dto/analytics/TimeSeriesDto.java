package ru.vvsem.bank.analyzer.dto.analytics;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TimeSeriesDto {

    @NotNull
    private LocalDate date;

    @NotNull
    private BigDecimal amount;
}
