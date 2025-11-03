package ru.vvsem.bank.analyzer.services.analytics;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {

    @Transactional(readOnly = true)
    List<TimeSeriesDto> getTimeSeries(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<Long> cardIdList,
            List<OperationType> operationTypeList,
            User user);

    List<CategoryBreakdownDto> getCategoryBreakdown(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<Long> cardIdList,
            List<OperationType> operationTypeList,
            User user);
}
