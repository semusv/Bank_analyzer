package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.mappers.OperationTypeMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.services.analytics.AnalyticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    private final AnalyticsService analyticsService;

    private final OperationTypeMapper operationTypeMapper;

    @RequestMapping("/time-series")
    @ResponseStatus(HttpStatus.OK)
    public List<TimeSeriesDto> getTimeSeries(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) List<Long> cardIdList,
            @RequestParam(required = false) List<Integer> operationTypeIdList,
            @AuthenticationPrincipal SecurityUser securityUser) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        List<OperationType> operationTypeList = operationTypeIdList == null
                ? null
                : operationTypeIdList.stream()
                .map(operationTypeMapper::mapOperationType)
                .filter(Objects::nonNull)
                .toList();
        return analyticsService.getTimeSeries(startDateTime, endDateTime, cardIdList, operationTypeList, securityUser);
    }


    @RequestMapping("/category-breakdown")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryBreakdownDto> getCategoryBreakdown(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) List<Long> cardIdList,
            @RequestParam(required = false) List<Integer> operationTypeIdList,
            @AuthenticationPrincipal SecurityUser securityUser) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        List<OperationType> operationTypeList = operationTypeIdList == null
                ? null
                : operationTypeIdList.stream()
                .map(operationTypeMapper::mapOperationType)
                .filter(Objects::nonNull)
                .toList();
        return analyticsService.getCategoryBreakdown(startDateTime, endDateTime, cardIdList, operationTypeList, securityUser);

    }
}

