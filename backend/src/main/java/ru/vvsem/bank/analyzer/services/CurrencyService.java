package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import java.util.List;

public interface CurrencyService {
    List<CurrencyDto> getAllCurrencies();
}
