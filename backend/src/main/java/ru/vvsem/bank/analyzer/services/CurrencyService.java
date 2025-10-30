package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.List;

public interface CurrencyService {
    List<CurrencyDto> getAllCurrencies();

    Currency getCurrencyByCardID(Long cardId);

    Currency getCurrencyById(Long id);
}
