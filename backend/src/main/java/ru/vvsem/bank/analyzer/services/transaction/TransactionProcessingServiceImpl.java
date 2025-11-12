package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.PatchTransactionData;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.exceptions.BusinessException;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.services.bank_account.BankAccountService;
import ru.vvsem.bank.analyzer.services.card.CardService;
import ru.vvsem.bank.analyzer.services.currency.CurrencyService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TransactionProcessingServiceImpl implements TransactionProcessingService {

    private final TransactionMapper transactionMapper;

    private final BankAccountService bankAccountService;

    private final EntityAccessProvider entityAccessProvider;

    private final CustomUserDetailsService userService;

    private final TransactionService transactionService;

    private final CardService cardService;

    private final CurrencyService currencyService;

    @SuppressWarnings("CheckStyle")
    @Override
    public List<TransactionDto> createTransactions(NewTransactionDto dto, SecurityUser securityUser) {
        List<TransactionDto> transactionDtoList = new ArrayList<>();
        Transaction revTransaction = null;
        User user = userService.getUserById(securityUser.getId());

        Transaction transaction = prepareNewTransaction(dto, user, dto.getCardId());
        if (dto.getRevCardId() != null) {
            revTransaction = prepareNewTransaction(dto, user, dto.getRevCardId());
            revTransaction.setAmount(revTransaction.getAmount().negate());
        }
        if (revTransaction != null) {
            if (!Objects.equals(revTransaction.getCurrency().getId(), transaction.getCurrency().getId())) {
                throw new BusinessException(
                        "Currency mismatch between cards",
                        "business.transaction.currency.mismatch"
                );
            }
            transactionDtoList.add(transactionService.createTransaction(revTransaction));
        }
        transactionDtoList.add(transactionService.createTransaction(transaction));
        transactionDtoList
                .forEach(transactionDto ->
                        bankAccountService.addAccountBalance(
                                transactionDto.getCardAccountId(),
                                transactionDto.getAmount(), securityUser));
        return transactionDtoList;
    }

    private Transaction prepareNewTransaction(
            NewTransactionDto newTransactionDto,
            User user,
            @NotNull(message = "{validation.Transaction.cardId.NotNull}") Long cardId) {
        Transaction transaction = new Transaction();
        transaction.setDescription(newTransactionDto.getDescription());
        transaction.setAmount(newTransactionDto.getAmount());
        transaction.setOperationTime(newTransactionDto.getOperationTime());
        transaction.setCategory(entityAccessProvider.getOwnedCategory(
                newTransactionDto.getCategoryId(),
                user.getId()));
        transaction.setCard(entityAccessProvider.getOwnedCard(cardId, user.getId()));
        transaction.setCurrency(currencyService.findByCardId(transaction.getCard().getId()));
        transaction.setOperationType(newTransactionDto.getOperationType());
        if (transaction.getOperationType() == OperationType.OUTGOING) {
            transaction.setAmount(transaction.getAmount().negate());
        }
        transaction.setUser(user);

        return transaction;
    }

    @SuppressWarnings("CheckStyle")
    @Override
    public void splitTransaction(
            Long transactionId, List<SubTransactionDto> subTransactions, SecurityUser securityUser) {
        Transaction parentTransaction = entityAccessProvider
                .getOwnedTransaction(transactionId, securityUser.getId());
        BigDecimal totalSubAmount = subTransactions.stream()
                .map(SubTransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (!transactionService.getByParentTransactionId(parentTransaction.getId()).isEmpty()) {
            throw new BusinessException(
                    "Can't split already splitted transaction",
                    "business.transaction.already.splitted"
            );
        }
        if (parentTransaction.getParentTransaction() != null) {
            throw new BusinessException(
                    "Can't split sub transaction",
                    "business.transaction.cant.split.sub"
            );
        }
        if (!totalSubAmount.equals(parentTransaction.getAmount())) {
            throw new IllegalArgumentException("Сумма дочерних транзакций не равна сумме родительской");
        }

        parentTransaction.setHide(true);
        parentTransaction.setMaster(true);
        transactionService.updateTransaction(parentTransaction, securityUser.getId());

        for (SubTransactionDto subTransactionDto : subTransactions) {
            if (subTransactionDto.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                Transaction subTransaction = getSubTransaction(subTransactionDto, parentTransaction);
                transactionService.createTransaction(subTransaction);
            }
        }
    }

    @Override
    public TransactionDto hideTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser) {
        Transaction transaction = entityAccessProvider.getOwnedTransaction(transactionId, securityUser.getId());

        if (!transactionService.getByParentTransactionId(transaction.getId()).isEmpty()) {
            throw new BusinessException(
                    "Can't unHide already splitted transaction",
                    "business.transaction.unHide.already.splitted"
            );
        }

        var transactionDto = transactionService.hideTransaction(transactionId, securityUser.getId());
        var account = entityAccessProvider.getOwnedBankAccount(transactionDto.getCardAccountId(), securityUser.getId());
        bankAccountService.addAccountBalance(account.getId(),
                !transactionDto.isHide()
                        ? transactionDto.getAmount()
                        : transactionDto.getAmount().negate(), securityUser);
        return transactionDto;
    }

    @Override
    public void deleteTransactionWithBalanceUpdate(Long transactionId, SecurityUser securityUser) {
        var transaction = entityAccessProvider.getOwnedTransaction(transactionId, securityUser.getId());
        var card = entityAccessProvider.getOwnedCard(transaction.getCard().getId(), securityUser.getId());

        if (!transactionService.getByParentTransactionId(transaction.getId()).isEmpty()) {
            throw new BusinessException(
                    "Can't delete already splitted transaction",
                    "business.transaction.delete.already.splitted"
            );
        }
        transactionService.deleteTransaction(transactionId, securityUser);
        bankAccountService.addAccountBalance(card.getAccount().getId(), transaction.getAmount().negate(), securityUser);
    }

    @Override
    public TransactionDto patchTransactionWithBalanceUpdate(
            Long transactionId, PatchTransactionData patchTransactionData, SecurityUser securityUser) {
        var transaction = entityAccessProvider.getOwnedTransaction(transactionId, securityUser.getId());
        var card = entityAccessProvider.getOwnedCard(transaction.getCard().getId(), securityUser.getId());
        var diffAmount = transaction.getAmount().subtract(patchTransactionData.getAmount());
        transaction.setCategory(entityAccessProvider
                .getOwnedCategory(patchTransactionData.getCategoryId(), securityUser.getId()));
        transaction.setAmount(patchTransactionData.getAmount());
        transaction.setOperationTime(patchTransactionData.getOperationTime());
        if (!diffAmount.equals(BigDecimal.ZERO)) {
            bankAccountService.addAccountBalance(card.getAccount().getId(), diffAmount.negate(), securityUser);
        }
        return transactionService.updateTransaction(transaction, securityUser.getId());
    }

    private static Transaction getSubTransaction(SubTransactionDto subTransactionDto, Transaction parentTransaction) {
        Transaction subTransaction = new Transaction();
        subTransaction.setAmount(subTransactionDto.getAmount());
        subTransaction.setDescription(subTransactionDto.getDescription());
        subTransaction.setOperationTime(parentTransaction.getOperationTime());
        subTransaction.setCategory(parentTransaction.getCategory());
        subTransaction.setCard(parentTransaction.getCard());
        subTransaction.setMaster(false);
        subTransaction.setHide(false);
        subTransaction.setParentTransaction(parentTransaction);
        subTransaction.setUser(parentTransaction.getUser());
        subTransaction.setOperationType(parentTransaction.getOperationType());
        subTransaction.setCategory(parentTransaction.getCategory());
        subTransaction.setCurrency(parentTransaction.getCurrency());
        return subTransaction;
    }

}
