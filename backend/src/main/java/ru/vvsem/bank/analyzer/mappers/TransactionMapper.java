package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.models.Transaction;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {BankMapper.class,
                CardMapper.class})
public interface TransactionMapper {
    @Mapping(source = "parentTransactionId", target = "parentTransaction.id")
    @Mapping(source = "currencyCode", target = "currency.code")
    @Mapping(source = "cardAccountId", target = "card.account.id")
    @Mapping(source = "cardId", target = "card.id")
    @Mapping(source = "bankId", target = "card.account.bank.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "bankCode", target = "card.account.bank.bankCode")
    Transaction toEntity(TransactionDto transactionDto);

    @AfterMapping
    default void linkSubTransactions(@MappingTarget Transaction transaction) {
        if (transaction.getSubTransactions() != null) {
            transaction.getSubTransactions()
                    .forEach(subTransaction -> subTransaction.setParentTransaction(transaction));
        }
    }

    @Mapping(source = "parentTransaction.id", target = "parentTransactionId")
    @Mapping(source = "currency.code", target = "currencyCode")
    @Mapping(source = "card.account.id", target = "cardAccountId")
    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "card.account.bank.id", target = "bankId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "card.account.bank.bankCode", target = "bankCode")
    TransactionDto toTransactionDto(Transaction transaction);

    Transaction toEntity(SubTransactionDto subTransactionDto);

    @InheritInverseConfiguration(name = "toEntity")
    SubTransactionDto toSubTransactionDto(Transaction transaction);

    @Mapping(source = "cardId", target = "card.id")
    @Mapping(source = "categoryId", target = "category.id")
    Transaction toEntity(NewTransactionDto newTransactionDto);

    @InheritInverseConfiguration(name = "toEntity")
    NewTransactionDto toNewTransactionDto(Transaction transaction);


    @Mapping(source = "parentTransactionId", target = "parentTransaction.id")
    @Mapping(source = "currencyCode", target = "currency.code")
    @Mapping(source = "cardAccountId", target = "card.account.id")
    @Mapping(source = "cardId", target = "card.id")
    @Mapping(source = "bankId", target = "card.account.bank.id")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "bankCode", target = "card.account.bank.bankCode")
    Transaction toEntity(TransactionDtoWithSiblings transactionDtoWithSiblings);

    @InheritInverseConfiguration(name = "toEntity")
    TransactionDtoWithSiblings toTransactionDtoWithSiblings(Transaction transaction);
}