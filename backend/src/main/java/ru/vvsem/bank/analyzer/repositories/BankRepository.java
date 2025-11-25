package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vvsem.bank.analyzer.models.Bank;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
}