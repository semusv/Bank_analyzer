package ru.vvsem.bank.analyzer.services.analytics;

import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface DashboardService {

    DashboardStatsDto getDashboardStats(SecurityUser securityUser);


    List<TransactionDto> getRecentTransactions(SecurityUser securityUser, int limit);
}
