package ru.vvsem.bank.analyzer.repositories;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>,
        JpaSpecificationExecutor<Transaction> {

    @Override
    @Nonnull
    @EntityGraph(value = "transaction-with-all-relations", type = EntityGraph.EntityGraphType.LOAD)
    Page<Transaction> findAll(Specification<Transaction> spec, @Nonnull Pageable pageable);

    @EntityGraph(value = "transaction-with-base-attributes", type = EntityGraph.EntityGraphType.LOAD)
    List<Transaction> findByParentTransactionId(Long parentTransactionId);

    @Override
    @Nonnull
    @EntityGraph(value = "transaction-with-base-attributes", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Transaction> findById(@Nonnull Long aLong);

    @EntityGraph(value = "transaction-with-base-attributes", type = EntityGraph.EntityGraphType.LOAD)
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
           "GROUP BY t.currency.code " +
           "ORDER BY t.currency.code ASC")
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
           "WHERE t.user.id = :userId and t.hide = false " +
           "ORDER BY t.operationTime DESC ")
    List<Transaction> findTopNByUserIdOrderByOperationTimeDesc(
            @Param("userId") Long userId,
            Pageable pageable);
}