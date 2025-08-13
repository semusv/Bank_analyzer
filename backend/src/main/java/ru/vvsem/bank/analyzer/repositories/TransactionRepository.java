package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByOperationTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByDescriptionContainingIgnoreCase(String description);

    List<Transaction> findByAmountGreaterThan(BigDecimal amount);

    List<Transaction> findByCurrencyCode(String code);

    List<Transaction> findByUserIdAndOperationTimeBetween(
            long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}