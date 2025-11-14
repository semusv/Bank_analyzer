package ru.vvsem.bank.analyzer.services.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.DashboardStatsDto;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    private final BankAccountRepository bankAccountRepository;

    private final TransactionMapper transactionMapper;

    private final ExchangeRateService exchangeRateService;

    private final CustomUserDetailsService userService;

    @Transactional(readOnly = true)
    @Override
    public DashboardStatsDto getDashboardStats(SecurityUser securityUser) {
        User user = userService.getUserById(securityUser.getId());
        log.info("Getting dashboard stats for user: {}", user.getId());
        LocalDateTime startOfMonth = getStartOfMonth();
        LocalDateTime endOfMonth = getEndOfMonth();

        List<CurrencyAmountDto> monthlyIncome = transactionRepository.calculateMonthlyIncome(
                user.getId(), startOfMonth, endOfMonth);
        List<CurrencyAmountDto> monthlyExpense = transactionRepository.calculateMonthlyExpense(
                user.getId(), startOfMonth, endOfMonth);
        List<CurrencyAmountDto> totalBalances = bankAccountRepository.calculateTotalBalanceByUserId(user.getId());

        return DashboardStatsDto.builder()
                .monthlyIncomeRub(exchangeRateService.convertListToRub(monthlyIncome))
                .monthlyIncomes(monthlyIncome)
                .monthlyExpenseRub(exchangeRateService.convertListToRub(monthlyExpense))
                .monthlyExpenses(monthlyExpense)
                .totalBalanceRub(exchangeRateService.convertListToRub(totalBalances))
                .totalBalances(totalBalances)
                .totalTransactions(getSafeLong(
                        transactionRepository.countByUserId(user.getId())))
                .uncategorizedTransactions(getSafeLong(
                        transactionRepository.countUncategorizedByUserId(user.getId())))
                .build();
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
    public List<TransactionDto> getRecentTransactions(SecurityUser securityUser, int limit) {
        User user = userService.getUserById(securityUser.getId());
        var transactions = transactionRepository
                .findTopNByUserIdOrderByOperationTimeDesc(
                        user.getId(),
                        PageRequest.of(0, limit));
        return transactions
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }
}