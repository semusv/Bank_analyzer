package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.models.Transaction;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        BankMapper.class, CardMapper.class})
public interface TransactionMapper {
    @Mapping(source = "userId", target = "user.id")
    Transaction toEntity(TransactionDto transactionDto);

    @AfterMapping
    default void linkSubTransactions(@MappingTarget Transaction transaction) {
        if (transaction.getSubTransactions() != null) {
            transaction.getSubTransactions()
                    .forEach(subTransaction -> subTransaction.setParentTransaction(transaction));
        }
    }

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "card.account.bank", target = "bank")
    TransactionDto toTransactionDto(Transaction transaction);

    Transaction toEntity(SubTransactionDto subTransactionDto);

    @InheritInverseConfiguration(name = "toEntity")
    SubTransactionDto toSubTransactionDto(Transaction transaction);
}