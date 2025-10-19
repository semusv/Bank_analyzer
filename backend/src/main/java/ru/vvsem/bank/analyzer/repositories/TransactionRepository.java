package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);

    @Query("SELECT COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.category IS NULL")
    Long countUncategorizedByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.amount > 0 AND t.operationTime BETWEEN :start AND :end")
    BigDecimal calculateMonthlyIncome(@Param("userId") Long userId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.amount) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.amount < 0 AND t.operationTime BETWEEN :start AND :end")
    BigDecimal calculateMonthlyExpense(@Param("userId") Long userId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT t " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId ORDER BY t.operationTime DESC LIMIT :limit")
    List<Transaction> findTopNByUserIdOrderByOperationTimeDesc(@Param("userId") Long userId, @Param("limit") int limit);
}