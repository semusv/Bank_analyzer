package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.validation.Valid;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.PatchTransactionData;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface TransactionProcessingService {

    List<TransactionDto> createTransactions(NewTransactionDto dto, SecurityUser securityUser);

    void splitTransaction(Long transactionId, List<SubTransactionDto> subTransactions, SecurityUser securityUser);

    TransactionDto hideTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser);

    void deleteTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser);

    TransactionDto patchTransactionWithBalanceUpdate(
            Long transactionId, @Valid PatchTransactionData patchTransactionData, SecurityUser securityUser);
}
