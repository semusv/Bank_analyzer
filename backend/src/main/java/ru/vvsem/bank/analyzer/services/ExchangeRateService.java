package ru.vvsem.bank.analyzer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.models.ExchangeRate;
import ru.vvsem.bank.analyzer.models.xml.ValCurs;
import ru.vvsem.bank.analyzer.models.xml.Valute;
import ru.vvsem.bank.analyzer.repositories.ExchangeRateRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository repository;

    public ExchangeRateService(ExchangeRateRepository repository) {
        this.repository = repository;
    }

    public void processExchangeRates(ValCurs valCurs) {
        log.info("Processing exchange rates for date: {}", valCurs.getDate());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        List<ExchangeRate> rates = valCurs.getValutes().stream()
                .map((Valute valute) -> convertToEntity(valute, LocalDate.parse(valCurs.getDate(),formatter)))
                .toList();

        repository.saveAll(rates);
        log.info("Saved {} exchange rates", rates.size());
    }

    private ExchangeRate convertToEntity(Valute valute, LocalDate date) {
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrencyCode(valute.getCharCode());
        rate.setCurrencyDate(date);
        rate.setCurrencyName(valute.getName());
        rate.setNominal(valute.getNominal());
        rate.setValue(valute.getDecimalValue());
        rate.setVunitRate(valute.getDecimalVunitRate());
        return rate;
    }
}
