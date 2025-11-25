package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.auth.RegisterFormDto;
import ru.vvsem.bank.analyzer.models.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegisterFormMapper {
    User toEntity(RegisterFormDto registerFormDto);

    RegisterFormDto toRegisterFormDto(User user);
}