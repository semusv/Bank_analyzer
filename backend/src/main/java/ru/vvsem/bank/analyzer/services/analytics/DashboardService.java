package ru.vvsem.bank.analyzer.services.analytics;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

public interface DashboardService {
    @Transactional(readOnly = true)
    DashboardStatsDto getDashboardStats(User user);

    @Transactional(readOnly = true)
    List<TransactionDto> getRecentTransactions(User user, int limit);
}
