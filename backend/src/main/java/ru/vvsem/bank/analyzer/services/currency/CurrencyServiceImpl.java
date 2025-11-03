package ru.vvsem.bank.analyzer.services.currency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import ru.vvsem.bank.analyzer.mappers.CurrencyMapper;
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



}
