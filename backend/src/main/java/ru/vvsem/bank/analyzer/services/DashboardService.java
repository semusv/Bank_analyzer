package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.TransactionDto;

import java.util.List;

public interface DashboardService {
    @Transactional(readOnly = true)
    DashboardStatsDto getDashboardStats(Long userId);

    @Transactional(readOnly = true)
    List<TransactionDto> getRecentTransactions(Long userId, int limit);
}
