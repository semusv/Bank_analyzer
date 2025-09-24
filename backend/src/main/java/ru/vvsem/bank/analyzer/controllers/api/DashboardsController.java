package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.TransactionDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.DashboardService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardsController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getDashboardStats(@AuthenticationPrincipal User user) {
        try {
            log.info("GET /api/dashboard/stats for user: {}, {}", user.getUsername(),user.getId());
            DashboardStatsDto stats = dashboardService.getDashboardStats(user.getId());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting dashboard stats for user: {}", user.getUsername(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<List<TransactionDto>> getRecentTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            log.info("GET /api/dashboard/recent-transactions for user: {}, limit: {}", user.getUsername(), limit);
            List<TransactionDto> transactions = dashboardService.getRecentTransactions(user.getId(), limit);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting recent transactions for user: {}", user.getUsername(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
