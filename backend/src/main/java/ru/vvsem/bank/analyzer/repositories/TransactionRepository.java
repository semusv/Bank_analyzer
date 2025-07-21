package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}