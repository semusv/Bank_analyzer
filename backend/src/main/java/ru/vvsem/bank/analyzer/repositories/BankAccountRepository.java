package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}