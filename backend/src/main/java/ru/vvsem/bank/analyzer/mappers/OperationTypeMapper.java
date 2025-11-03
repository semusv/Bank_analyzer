package ru.vvsem.bank.analyzer.mappers;

import org.mapstruct.Mapper;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

@Mapper(componentModel = "spring")
public interface OperationTypeMapper {

    default OperationType mapOperationType(Integer operationTypeId) {
        if (operationTypeId == null) {
            return null;
        }

        OperationType[] values = OperationType.values();
        if (operationTypeId < 0 || operationTypeId >= values.length) {
            return null;
        }

        return values[operationTypeId];
    }
}
