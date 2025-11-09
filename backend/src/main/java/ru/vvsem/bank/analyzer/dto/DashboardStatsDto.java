package ru.vvsem.bank.analyzer.dto;

import lombok.Builder;
import lombok.Data;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardStatsDto {
    private BigDecimal totalBalanceRub;

    private List<CurrencyAmountDto> totalBalances;

    private BigDecimal monthlyIncomeRub;

    private List<CurrencyAmountDto> monthlyIncomes;

    private BigDecimal monthlyExpenseRub;

    private List<CurrencyAmountDto> monthlyExpenses;

    private Long totalTransactions;

    private Long uncategorizedTransactions;
}