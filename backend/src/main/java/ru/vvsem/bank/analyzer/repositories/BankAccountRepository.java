package ru.vvsem.bank.analyzer.repositories;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.BankAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    @Transactional
    @Modifying
    @Query("update BankAccount b set b.balance = ?1 where b.id = ?2")
    int updateBalance(BigDecimal balance, Long bankAccountId);

    @EntityGraph(attributePaths = {"card", "user", "currency", "bank"})
    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"card", "user", "currency", "bank"})
    List<BankAccount> findByUserId(Long userId);

    @Query("SELECT NEW ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto(b.currency.code, SUM(b.balance)) " +
           "FROM BankAccount b " +
           "WHERE b.user.id = :userId " +
           "GROUP BY b.currency.code " +
           "ORDER BY b.currency.code")
    List<CurrencyAmountDto> calculateTotalBalanceByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"card", "user", "currency", "bank"})
    List<BankAccount> findWithCardsAndUserAndCurrencyByUserId(Long userId);
}