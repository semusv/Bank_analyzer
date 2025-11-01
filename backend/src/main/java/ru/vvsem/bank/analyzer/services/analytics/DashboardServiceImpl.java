package ru.vvsem.bank.analyzer.services.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public DashboardStatsDto getDashboardStats(User user) {
        log.info("Getting dashboard stats for user: {}", user.getId());
        LocalDateTime startOfMonth = getStartOfMonth();
        LocalDateTime endOfMonth = getEndOfMonth();
        return DashboardStatsDto.builder()
                .monthlyIncome(getSafeBigDecimal(
                        transactionRepository.calculateMonthlyIncome(user.getId(), startOfMonth, endOfMonth)))
                .monthlyExpense(getSafeBigDecimal(
                        transactionRepository.calculateMonthlyExpense(user.getId(), startOfMonth, endOfMonth)))
                .totalBalance(getSafeBigDecimal(
                        bankAccountRepository.calculateTotalBalanceByUserId(user.getId())))
                .totalTransactions(getSafeLong(
                        transactionRepository.countByUserId(user.getId())))
                .uncategorizedTransactions(getSafeLong(
                        transactionRepository.countUncategorizedByUserId(user.getId())))
                .build();
    }

    private BigDecimal getSafeBigDecimal(BigDecimal value) {
        return value != null ? value.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
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
    public List<TransactionDto> getRecentTransactions(User user, int limit) {
        log.info("Getting recent {} transactions for user: {}", limit, user.getId());

        var transactions = transactionRepository.findTopNByUserIdOrderByOperationTimeDesc(user.getId(), limit);
        return transactions
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }
}