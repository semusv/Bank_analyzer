package ru.vvsem.bank.analyzer.services.analytics;

import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.SeriesFilterDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface AnalyticsService {


    List<TimeSeriesDto> getTimeSeries(
            SeriesFilterDto filter,
            SecurityUser securityUser);


    List<CategoryBreakdownDto> getCategoryBreakdown(
            SeriesFilterDto filter,
            SecurityUser securityUser);
}
