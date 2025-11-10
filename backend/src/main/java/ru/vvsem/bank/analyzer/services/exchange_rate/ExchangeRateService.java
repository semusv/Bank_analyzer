package ru.vvsem.bank.analyzer.services.exchange_rate;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.models.xml.ValCurs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExchangeRateService {
    void processExchangeRates(ValCurs valCurs);

    @Transactional(readOnly = true)
    boolean needLoadForDate(LocalDate date);

    @Transactional(readOnly = true)
    BigDecimal convertListToRub(List<CurrencyAmountDto> amounts);

    @Transactional(readOnly = true)
    BigDecimal convertToRub(BigDecimal amount, String currencyCode, LocalDate date);

    @Transactional(readOnly = true)
    BigDecimal convertToRub(BigDecimal amount, String currencyCode);
}
