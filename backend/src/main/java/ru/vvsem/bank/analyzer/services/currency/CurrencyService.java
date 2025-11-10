package ru.vvsem.bank.analyzer.services.currency;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;

import java.util.List;

public interface CurrencyService {
    @Transactional(readOnly = true)
    List<CurrencyDto> getAllCurrencies();
}
