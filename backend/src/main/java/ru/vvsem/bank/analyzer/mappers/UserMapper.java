package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.vvsem.bank.analyzer.dto.UserDto;
import ru.vvsem.bank.analyzer.models.User;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    User toEntity(UserDto userDto);

    @AfterMapping
    default void linkBankAccounts(@MappingTarget User user) {
        user.getBankAccounts().forEach(bankAccount -> bankAccount.setUser(user));
    }

    @AfterMapping
    default void linkCategories(@MappingTarget User user) {
        user.getCategories().forEach(category -> category.setUser(user));
    }


    UserDto toUserDto(User user);
}