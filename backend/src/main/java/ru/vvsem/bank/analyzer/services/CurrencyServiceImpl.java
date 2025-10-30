package ru.vvsem.bank.analyzer.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapper;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.repositories.CurrencyRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;

    private final CurrencyMapper currencyMapper;

    @Override
    public List<CurrencyDto> getAllCurrencies() {
        return currencyRepository.findAll()
                .stream()
                .map(currencyMapper::toCurrencyDto)
                .toList();
    }

    @Override
    public Currency getCurrencyByCardID(Long cardId) {
        return currencyRepository.getCurrencyByCardId(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(cardId),
                        "exception.entity.not.found.currency"));
    }

    @Override
    public Currency getCurrencyById(Long id) {
        return currencyRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "Currency with id %d not found".formatted(id),
                                        "exception.entity.not.found.currency")
                );
    }
}
