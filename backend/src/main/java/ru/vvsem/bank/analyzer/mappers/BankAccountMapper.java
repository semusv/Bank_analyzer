package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.dto.account.BankAccountWithCardsDto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CardMapper.class})
public interface BankAccountMapper {

    @AfterMapping
    default void linkCards(@MappingTarget BankAccount bankAccount) {
        bankAccount.getCards().forEach(card -> card.setAccount(bankAccount));
    }

    @Mapping(source = "currencySymbol", target = "currency.symbol")
    @Mapping(source = "currencyCode", target = "currency.code")
    @Mapping(source = "bankName", target = "bank.name")
    @Mapping(source = "bankId", target = "bank.id")
    @Mapping(source = "bankCode", target = "bank.bankCode")
    BankAccount toEntity(BankAccountWithCardsDto bankAccountWithCardsDto);

    @InheritInverseConfiguration(name = "toEntity")
    BankAccountWithCardsDto toBankAccountSimpleDto(BankAccount bankAccount);


    @Mapping(source = "currencyId", target = "currency.id")
    @Mapping(source = "bankId", target = "bank.id")
    @Mapping(source = "initialBalance", target = "balance")
    BankAccount toEntity(NewBankAccountDto newBankAccountDto);
}