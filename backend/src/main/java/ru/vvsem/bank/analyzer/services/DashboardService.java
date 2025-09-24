package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.BudgetRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats(Long userId) {
        log.info("Getting dashboard stats for user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        DashboardStatsDto stats = new DashboardStatsDto();

        // Общий баланс (сумма по всем счетам пользователя)
        BigDecimal totalBalance = bankAccountRepository.calculateTotalBalanceByUserId(userId);
        stats.setTotalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO);

        // Доходы за текущий месяц
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().getMonth().length(LocalDateTime.now().toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59);

        BigDecimal monthlyIncome = transactionRepository.calculateMonthlyIncome(userId, startOfMonth, endOfMonth);
        stats.setMonthlyIncome(monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO);

        // Расходы за текущий месяц
        BigDecimal monthlyExpense = transactionRepository.calculateMonthlyExpense(userId, startOfMonth, endOfMonth);
        stats.setMonthlyExpense(monthlyExpense != null ? monthlyExpense.abs() : BigDecimal.ZERO); // Берем модуль

        // Активные бюджеты
        Long activeBudgets = budgetRepository.countByUserId(userId);
        stats.setActiveBudgets(activeBudgets != null ? activeBudgets : 0L);

        // Общее количество транзакций
        Long totalTransactions = transactionRepository.countByUserId(userId);
        stats.setTotalTransactions(totalTransactions != null ? totalTransactions : 0L);

        // Транзакции без категории
        Long uncategorizedTransactions = transactionRepository.countUncategorizedByUserId(userId);
        stats.setUncategorizedTransactions(uncategorizedTransactions != null ? uncategorizedTransactions : 0L);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getRecentTransactions(Long userId, int limit) {
        log.info("Getting recent {} transactions for user: {}", limit, userId);

        var transactions =  transactionRepository.findTopNByUserIdOrderByOperationTimeDesc(userId, limit);
        var trDto =  transactions
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
        return trDto;
    }
}