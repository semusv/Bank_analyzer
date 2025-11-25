package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.SeriesFilterDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.services.analytics.AnalyticsService;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsApiController {

    private final AnalyticsService analyticsService;



    @RequestMapping("/time-series")
    @ResponseStatus(HttpStatus.OK)
    public List<TimeSeriesDto> getTimeSeries(
            @ModelAttribute SeriesFilterDto filter,
            @AuthenticationPrincipal SecurityUser securityUser) {

        return analyticsService.getTimeSeries(filter, securityUser);
    }


    @RequestMapping("/category-breakdown")
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryBreakdownDto> getCategoryBreakdown(
            @ModelAttribute SeriesFilterDto filter,
            @AuthenticationPrincipal SecurityUser securityUser) {


        return analyticsService.getCategoryBreakdown(
                filter,
                securityUser);

    }
}

