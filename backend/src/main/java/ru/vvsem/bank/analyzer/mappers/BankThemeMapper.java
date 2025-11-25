package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.BankThemeDto;
import ru.vvsem.bank.analyzer.models.BankTheme;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BankThemeMapper {

    BankThemeDto toBankThemeDto(BankTheme bankTheme);
}
