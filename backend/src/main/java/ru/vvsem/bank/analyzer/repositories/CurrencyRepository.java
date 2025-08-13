package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Optional<Currency> findByCode(String code);
}