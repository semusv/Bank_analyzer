package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {
    Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);

    List<Transaction> findByUserId(Long userId);

    @Query("SELECT COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.category IS NULL")
    Long countUncategorizedByUserId(@Param("userId") Long userId);

    @Query("SELECT NEW ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto(t.currency.code, SUM(t.amount)) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.amount > 0 AND t.operationTime BETWEEN :start AND :end " +
           "GROUP BY t.currency.code")
    List<CurrencyAmountDto> calculateMonthlyIncome(@Param("userId") Long userId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT  NEW ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto(t.currency.code, SUM(t.amount)) " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.amount < 0 AND t.operationTime BETWEEN :start AND :end " +
           "GROUP BY t.currency.code")
    List<CurrencyAmountDto> calculateMonthlyExpense(@Param("userId") Long userId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT t " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId ORDER BY t.operationTime DESC LIMIT :limit")
    List<Transaction> findTopNByUserIdOrderByOperationTimeDesc(@Param("userId") Long userId, @Param("limit") int limit);
}