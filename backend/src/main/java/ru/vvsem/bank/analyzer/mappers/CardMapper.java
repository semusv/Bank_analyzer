package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.CardDto;
import ru.vvsem.bank.analyzer.models.Card;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    Card toEntity(CardDto cardDto);

    CardDto toCardDto(Card card);
}