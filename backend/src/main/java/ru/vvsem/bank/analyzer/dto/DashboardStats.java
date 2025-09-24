package ru.vvsem.bank.analyzer.dto;


import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStats {
    private BigDecimal totalBalance;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private Long activeBudgets;
    private Long totalTransactions;
    private Long uncategorizedTransactions;
}