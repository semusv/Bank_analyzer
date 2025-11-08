package ru.vvsem.bank.analyzer.dto.analytics;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SeriesFilterDto {

    private LocalDate startDate;

    private LocalDate endDate;

    private List<Long> cardIdList;

    private List<Integer> operationTypeIdList;

    public LocalDateTime getStartDateTime() {
        return startDate != null ? startDate.atStartOfDay() : null;
    }

    public LocalDateTime getEndDateTime() {
        return endDate != null ? endDate.atTime(23, 59, 59) : null;
    }

}
