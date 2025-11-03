package ru.vvsem.bank.analyzer.services.currency;

import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;

import java.util.List;

public interface CurrencyService {
    List<CurrencyDto> getAllCurrencies();
}
