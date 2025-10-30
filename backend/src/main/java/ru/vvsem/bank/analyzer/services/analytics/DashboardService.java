package ru.vvsem.bank.analyzer.services.analytics;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;

import java.util.List;

public interface DashboardService {
    @Transactional(readOnly = true)
    DashboardStatsDto getDashboardStats(Long userId);

    @Transactional(readOnly = true)
    List<TransactionDto> getRecentTransactions(Long userId, int limit);
}
