package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.BankAccount;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUserId(Long id);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

}