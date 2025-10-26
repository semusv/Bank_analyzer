package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.Accounts.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.dto.CardDto;
import ru.vvsem.bank.analyzer.models.Card;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    Card toEntity(CardDto cardDto);

    CardDto toCardDto(Card card);

    @Mapping(source = "cardName", target = "cardName")
    @Mapping(source = "lastFourDigits", target = "lastFourDigits")
    @Mapping(source = "id", target = "id")
    Card toEntity(BankAccountSimpleDto.CardDto cardDto);

    @InheritInverseConfiguration(name = "toEntity")
    BankAccountSimpleDto.CardDto toCardDto1(Card card);
}