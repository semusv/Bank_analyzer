package ru.vvsem.bank.analyzer.services.exchange_rate;

import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.xml.ValCurs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExchangeRateService {
    void processExchangeRates(ValCurs valCurs);

    boolean needLoadForDate(LocalDate date);

    BigDecimal convertListToRub(List<CurrencyAmountDto> amounts);

    BigDecimal convertToRub(BigDecimal amount, String currencyCode, LocalDate date);

    BigDecimal convertToRub(BigDecimal amount, String currencyCode);
}
