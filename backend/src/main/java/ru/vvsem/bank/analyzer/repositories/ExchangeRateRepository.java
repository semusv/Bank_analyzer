package ru.vvsem.bank.analyzer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vvsem.bank.analyzer.models.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findFirstByCurrencyCodeAndCurrencyDateLessThanEqualOrderByCurrencyDateAsc(String currencyCode, LocalDate currencyDate);

    Optional<ExchangeRate> findByCurrencyCodeAndCurrencyDate(String currencyCode, LocalDate currencyDate);

    Optional<ExchangeRate> findFirstByCurrencyCodeOrderByCurrencyDateDesc(String currencyCode);

    boolean existsByCurrencyDate(LocalDate currencyDate);


}