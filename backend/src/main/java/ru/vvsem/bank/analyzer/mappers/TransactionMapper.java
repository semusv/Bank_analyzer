package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.TransactionDto;
import ru.vvsem.bank.analyzer.models.Transaction;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(source = "userId", target = "user.id")
    Transaction toEntity(TransactionDto transactionDto);

    @AfterMapping
    default void linkSubTransactions(@MappingTarget Transaction transaction) {
        transaction.getSubTransactions().forEach(subTransaction -> subTransaction.setParentTransaction(transaction));
    }

    @Mapping(source = "user.id", target = "userId")
    TransactionDto toTransactionDto(Transaction transaction);
}