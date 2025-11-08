package ru.vvsem.bank.analyzer.dto.transaction;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransactionFilterDto {

    private LocalDate startDate;

    private LocalDate endDate;

    private Long cardId;

    private Long bankId;

    private Long categoryId;

    private String description;

    public LocalDateTime getStartDateTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }

    public LocalDateTime getEndDateTime() {
        return endDate != null ? endDate.atTime(23, 59, 59) : null;
    }

}
