package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vvsem.bank.analyzer.models.Budget;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Budget> findByUserId(Long userId);

    @Query("SELECT COUNT(b) FROM Budget b WHERE b.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}