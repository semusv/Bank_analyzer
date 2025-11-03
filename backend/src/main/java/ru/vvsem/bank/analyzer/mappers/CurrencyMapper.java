package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.currency.CurrencyDto;
import ru.vvsem.bank.analyzer.models.Currency;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CurrencyMapper {
    Currency toEntity(CurrencyDto currencyDto);

    CurrencyDto toCurrencyDto(Currency currency);
}