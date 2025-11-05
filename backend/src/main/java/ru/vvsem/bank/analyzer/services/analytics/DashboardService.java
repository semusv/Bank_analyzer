package ru.vvsem.bank.analyzer.services.analytics;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface DashboardService {
    @Transactional(readOnly = true)
    DashboardStatsDto getDashboardStats(SecurityUser securityUser);

    @Transactional(readOnly = true)
    List<TransactionDto> getRecentTransactions(SecurityUser securityUser, int limit);
}
