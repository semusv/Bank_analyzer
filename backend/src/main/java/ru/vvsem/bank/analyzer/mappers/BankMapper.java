package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.BankDto;
import ru.vvsem.bank.analyzer.models.Bank;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankMapper {
    Bank toEntity(BankDto bankDto);

    BankDto toBankDto(Bank bank);
}