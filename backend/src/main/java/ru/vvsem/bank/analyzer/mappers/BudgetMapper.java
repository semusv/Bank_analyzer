package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.BudgetDto;
import ru.vvsem.bank.analyzer.models.Budget;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BudgetMapper {
    @Mapping(source = "userId", target = "user.id")
    Budget toEntity(BudgetDto budgetDto);

    @Mapping(source = "user.id", target = "userId")
    BudgetDto toBudgetDto(Budget budget);
}