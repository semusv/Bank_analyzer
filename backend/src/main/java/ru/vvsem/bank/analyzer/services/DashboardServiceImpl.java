package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    private final BankAccountRepository bankAccountRepository;

    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    @Override
    public DashboardStatsDto getDashboardStats(Long userId) {
        log.info("Getting dashboard stats for user: {}", userId);
        LocalDateTime startOfMonth = getStartOfMonth();
        LocalDateTime endOfMonth = getEndOfMonth();
        return DashboardStatsDto.builder()
                .totalBalance(getSafeBigDecimal(
                        bankAccountRepository.calculateTotalBalanceByUserId(userId)))
                .monthlyIncome(getSafeBigDecimal(
                        transactionRepository.calculateMonthlyIncome(userId, startOfMonth, endOfMonth)))
                .monthlyExpense(getSafeBigDecimal(
                        transactionRepository.calculateMonthlyExpense(userId, startOfMonth, endOfMonth)).abs())
                .totalTransactions(getSafeLong(
                        transactionRepository.countByUserId(userId)))
                .uncategorizedTransactions(getSafeLong(
                        transactionRepository.countUncategorizedByUserId(userId)))
                .build();
    }

    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Long getSafeLong(Long value) {
        return value != null ? value : 0L;
    }

    private static LocalDateTime getEndOfMonth() {
        return LocalDateTime.now()
                .withDayOfMonth(LocalDateTime.now().getMonth().length(LocalDateTime.now().toLocalDate().isLeapYear()))
                .withHour(23)
                .withMinute(59).withSecond(59);
    }

    private static LocalDateTime getStartOfMonth() {
        return LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionDto> getRecentTransactions(Long userId, int limit) {
        log.info("Getting recent {} transactions for user: {}", limit, userId);

        var transactions = transactionRepository.findTopNByUserIdOrderByOperationTimeDesc(userId, limit);
        return transactions
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }
}