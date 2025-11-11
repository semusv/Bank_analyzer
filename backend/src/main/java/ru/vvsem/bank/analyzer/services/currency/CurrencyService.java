package ru.vvsem.bank.analyzer.services.currency;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.models.Currency;

import java.util.List;

public interface CurrencyService {
    @Transactional(readOnly = true)
    List<CurrencyDto> getAllCurrencyDto();

    @Transactional(readOnly = true)
    Currency findByCardId(Long cardId);

    @Transactional(readOnly = true)
    Currency findById(Long currencyId);
}
