package ru.vvsem.bank.analyzer.mappers.transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.mappers.BankMapper;
import ru.vvsem.bank.analyzer.mappers.CardMapper;
import ru.vvsem.bank.analyzer.models.Transaction;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {BankMapper.class, CardMapper.class}
)
public interface NewTransactionMapper {

    @Mapping(source = "cardId", target = "card.id")
    @Mapping(source = "categoryId", target = "category.id")
    Transaction toEntity(NewTransactionDto dto);

    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "category.id", target = "categoryId")
    NewTransactionDto toDto(Transaction transaction);
}