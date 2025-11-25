package ru.vvsem.bank.analyzer.services.currency;

import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.List;

public interface CurrencyService {

    List<CurrencyDto> getAllCurrencyDto();

    Currency findByCardId(Long cardId);

    Currency findById(Long currencyId);
}
