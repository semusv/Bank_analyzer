package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.BankAccountDto;
import ru.vvsem.bank.analyzer.models.BankAccount;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankAccountMapper {
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "currencyCode", target = "currency.code")
    @Mapping(source = "currencyId", target = "currency.id")
    BankAccount toEntity(BankAccountDto bankAccountDto);

    @AfterMapping
    default void linkCards(@MappingTarget BankAccount bankAccount) {
        bankAccount.getCards().forEach(card -> card.setAccount(bankAccount));
    }

    @InheritInverseConfiguration(name = "toEntity")
    BankAccountDto toBankAccountDto(BankAccount bankAccount);
}