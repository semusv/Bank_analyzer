package ru.vvsem.bank.analyzer.services.transaction;

import org.springframework.data.domain.Page;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionFilterDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

public interface TransactionSearchService {


    Page<TransactionDtoWithSiblings> searchTransactions(
            SecurityUser securityUser,
            TransactionFilterDto filter,
            int page,
            int size);
}
