package ru.vvsem.bank.analyzer.services.exchange_rate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyAmountDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.models.ExchangeRate;
import ru.vvsem.bank.analyzer.models.xml.ValCurs;
import ru.vvsem.bank.analyzer.models.xml.Valute;
import ru.vvsem.bank.analyzer.repositories.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final String CURRENCY_CODE_RUB = "RUB";

    private final Map<LocalDate, Map<String, ExchangeRate>> rateCache = new HashMap<>();

    private final ExchangeRateRepository exchangeRateRepository;

    private final RequestToViewNameTranslator requestToViewNameTranslator;

    @Override
    public void processExchangeRates(ValCurs valCurs) {
        log.info("Processing exchange rates for date: {}", valCurs.getDate());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(valCurs.getDate(), formatter);
        List<ExchangeRate> rates = valCurs.getValutes().stream()
                .map((Valute valute) -> convertToEntity(valute, date))
                .toList();

        if (exchangeRateRepository.existsByCurrencyDate(date)) {
            log.info("Exchange rates for date {} already exist", date);
            return;
        }

        exchangeRateRepository.saveAll(rates);
        log.info("Saved {} exchange rates", rates.size());
    }

    @Override
    public boolean needLoadForDate(LocalDate date) {
        return !exchangeRateRepository.existsByCurrencyDate(date);
    }

    @Override
    public BigDecimal convertListToRub(List<CurrencyAmountDto> amounts) {
        if (amounts == null || amounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return amounts.stream()
                .map(dto -> convertToRub(dto.getAmount(), dto.getCurrencyCode()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //    convertToRub(currencyAmountDto.getAmount(), currencyAmountDto.getCurrencyCode()
    @Override
    public BigDecimal convertToRub(BigDecimal amount, String currencyCode, LocalDate date) {
        if (CURRENCY_CODE_RUB.equals(currencyCode)) {
            return amount;
        }
        ExchangeRate rate = getRateForDate(currencyCode, date);
        return convertWithRate(amount, rate);
    }

    @Override
    public BigDecimal convertToRub(BigDecimal amount, String currencyCode) {
        if (CURRENCY_CODE_RUB.equals(currencyCode)) {
            return amount;
        }

        ExchangeRate rate = getRateForDate(currencyCode, LocalDate.now());
        return convertWithRate(amount, rate);
    }

    private BigDecimal convertWithRate(BigDecimal amount, ExchangeRate rate) {
        return amount
                .multiply(rate.getValue())
                .divide(
                        BigDecimal.valueOf(rate.getNominal()),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private ExchangeRate getRateForDate(String currencyCode, LocalDate date) {
        ExchangeRate rate = rateCache.getOrDefault(date, Map.of()).get(currencyCode);
        if (rate == null) {
            rate = exchangeRateRepository
                    .findFirstByCurrencyCodeAndCurrencyDateLessThanEqualOrderByCurrencyDateAsc(currencyCode, date)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Rate for code %s and date %s (less than equal) not found".formatted(currencyCode, date),
                            "exception.entity.not.found.exchangerate"));
            rateCache.computeIfAbsent(date, k -> new HashMap<>())
                    .put(currencyCode, rate);
        }
        return rate;
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
