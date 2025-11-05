package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.analytics.DashboardService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardsApiController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public DashboardStatsDto getDashboardStats(@AuthenticationPrincipal SecurityUser securityUser) {
        return dashboardService.getDashboardStats(securityUser);
    }

    @GetMapping("/recent-transactions")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getRecentTransactions(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "5") int limit) {
        return dashboardService.getRecentTransactions(securityUser, limit);
    }

}
