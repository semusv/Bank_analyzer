package ru.vvsem.bank.analyzer.services.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final EntityAccessProviderImpl entityAccessProviderImpl;

    private final CustomUserDetailsService userService;

    @Override
    public List<TransactionDto> getUserTransactionDtoList(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }

    @Override
    public TransactionDto getTransactionDtoByIdAndUserId(Long transactionId, Long userId) {
        return transactionMapper.toTransactionDto(
                entityAccessProviderImpl.getOwnedTransaction(transactionId, userId));
    }


    @Override
    public void deleteTransaction(Long transactionId, SecurityUser securityUser) {
        Transaction transaction = entityAccessProviderImpl.getOwnedTransaction(transactionId, securityUser.getId());
        transactionRepository.delete(transaction);
    }

    @Override
    public Page<TransactionDto> searchTransactions(Specification<Transaction> criteria, Pageable pageable) {
        Page<Transaction> transactions = transactionRepository.findAll(criteria, pageable);
        return transactions.map(transactionMapper::toTransactionDto);
    }

    @Override
    public TransactionDto hideTransaction(Long transactionId, Long userId) {
        Transaction transaction = entityAccessProviderImpl.getOwnedTransaction(transactionId, userId);
        transaction.setHide(!transaction.isHide());
        return transactionMapper.toTransactionDto(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDto updateTransaction(Transaction transaction, Long userId) {
        entityAccessProviderImpl.getOwnedTransaction(transaction.getId(), userId);
        return transactionMapper.toTransactionDto(transactionRepository.save(transaction));
    }

    @Override
    public TransactionDto createTransaction(Transaction transaction) {
        return transactionMapper.toTransactionDto(transactionRepository.save(transaction));
    }

    @Override
    public List<TransactionDto> getByParentTransactionId(Long parentTransactionId) {
        return transactionRepository.findByParentTransactionId(parentTransactionId)
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }
}
