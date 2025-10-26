package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.Card;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    @Mapping(source = "issuerBank.id", target = "issuerBankId")
    @Mapping(source = "account.id", target = "accountId")
    CardDto toCardDto(Card card);

    @Mapping(source = "issuerBankId", target = "issuerBank.id")
    @Mapping(source = "accountId", target = "account.id")
    Card toEntity(CardDto cardDto);

    @Mapping(source = "accountId", target = "account.id")
    Card toEntity(NewCardDto newCardDto);
}