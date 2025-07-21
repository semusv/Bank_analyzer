package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Budget;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
}