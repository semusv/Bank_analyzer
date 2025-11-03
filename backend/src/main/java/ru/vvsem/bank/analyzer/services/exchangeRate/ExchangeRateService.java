package ru.vvsem.bank.analyzer.services.exchangeRate;

import ru.vvsem.bank.analyzer.models.xml.ValCurs;

import java.time.LocalDate;

public interface ExchangeRateService {
    void processExchangeRates(ValCurs valCurs);

    boolean needLoadForDate(LocalDate date);
}
