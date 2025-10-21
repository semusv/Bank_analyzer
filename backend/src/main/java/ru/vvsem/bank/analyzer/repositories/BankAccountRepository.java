package ru.vvsem.bank.analyzer.repositories;

import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByIdAndUserId(Long id, Long userId);

    List<BankAccount> findByUserId(Long userId);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    @Query("SELECT SUM(b.balance) FROM BankAccount b WHERE b.user.id = :userId")
    BigDecimal calculateTotalBalanceByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"cards", "user", "currency", "bank"})
    List<BankAccount> findWithCardsAndUserAndCurrencyByUserId(Long userId);
}