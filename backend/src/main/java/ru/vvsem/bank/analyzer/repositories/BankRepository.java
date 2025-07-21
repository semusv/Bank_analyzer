package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Bank;

public interface BankRepository extends JpaRepository<Bank, Long> {
}