package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.PatchTransactionData;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface TransactionProcessingService {
    @Transactional
    public List<TransactionDto> createTransaction(NewTransactionDto dto, SecurityUser securityUser);

    @Transactional
    public void splitTransaction(Long transactionId, List<SubTransactionDto> subTransactions, SecurityUser securityUser);

    @Transactional
    public void hideTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser);

    @Transactional
    public void deleteTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser);

    @Transactional
    TransactionDto patchTransactionWithBalanceUpdate(Long transactionId, @Valid PatchTransactionData patchTransactionData, SecurityUser securityUser);
}
