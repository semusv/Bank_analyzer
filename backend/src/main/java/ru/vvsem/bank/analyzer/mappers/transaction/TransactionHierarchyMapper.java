package ru.vvsem.bank.analyzer.mappers.transaction;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.mappers.BankMapper;
import ru.vvsem.bank.analyzer.mappers.CardMapper;
import ru.vvsem.bank.analyzer.models.Transaction;

import java.util.ArrayList;
import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        uses = {BankMapper.class, CardMapper.class, TransactionMapper.class}
)
public interface TransactionHierarchyMapper {

    @Mapping(source = "parentTransaction.id", target = "parentTransactionId")
    @Mapping(source = "currency.code", target = "currencyCode")
    @Mapping(source = "card.account.id", target = "cardAccountId")
    @Mapping(source = "card.id", target = "cardId")
    @Mapping(source = "card.account.bank.id", target = "bankId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "card.account.bank.bankCode", target = "bankCode")
    @Mapping(target = "subTransactions", ignore = true) // ИГНОРИРУЕМ - обработаем в @AfterMapping
    TransactionDtoWithSiblings toDto(Transaction transaction);

    @AfterMapping
    default void populateSubTransactions(@MappingTarget TransactionDtoWithSiblings dto,
                                         Transaction transaction) {
        if (!transaction.isMaster()) {
            dto.setSubTransactions(null);
            return;
        }

        // Если транзакция мастер, то маппим подтранзакции
        List<TransactionDtoWithSiblings> subDtos = new ArrayList<>();
        if (transaction.getSubTransactions() != null) {
            for (Transaction sub : transaction.getSubTransactions()) {
                TransactionDtoWithSiblings subDto = toDto(sub);
                subDto.setParentTransactionId(transaction.getId());
                subDtos.add(subDto);
            }
        }
        dto.setSubTransactions(subDtos);
    }
}
