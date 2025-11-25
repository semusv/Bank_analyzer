package ru.vvsem.bank.analyzer.services.currency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapper;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.enums.EntityName;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.CurrencyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final CurrencyMapper currencyMapper;

    private final EntityAccessProvider entityAccessProvider;

    @Override
    @Transactional(readOnly = true)
    public List<CurrencyDto> getAllCurrencyDto() {
        return currencyRepository.findAll()
                .stream()
                .map(currencyMapper::toCurrencyDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Currency findByCardId(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException(
                    "Card id must not be null");
        }
        return currencyRepository.getCurrencyByCardId(cardId)
                .orElseThrow(() ->
                        entityAccessProvider.throwEntityNotFound(EntityName.CURRENCY, cardId));
    }

    @Override
    @Transactional(readOnly = true)
    public Currency findById(Long currencyId) {
        if (currencyId == null) {
            throw new IllegalArgumentException(
                    "Currency id must not be null");
        }
        return currencyRepository.findById(currencyId)
                .orElseThrow(() ->
                        entityAccessProvider.throwEntityNotFound(EntityName.CURRENCY, currencyId));
    }

}
