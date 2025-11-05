package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.providers.EntityAccessProviderImpl;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.bank_account.BankAccountService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final BankAccountService bankAccountService;

    private final EntityAccessProviderImpl entityAccessProviderImpl;

    private final CustomUserDetailsService userService;

    @Override
    public List<TransactionDto> getListTransaction(SecurityUser securityUser) {
        return transactionRepository.findByUserId(securityUser.getId())
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }

    @Override
    public TransactionDto getTransaction(Long transactionId, SecurityUser securityUser) {
        return transactionMapper.toTransactionDto(
                entityAccessProviderImpl.requireOwnedTransaction(transactionId, securityUser.getId()));
    }

    @Override
    public void hideTransaction(Long transactionId, SecurityUser securityUser) {
        Transaction transaction = entityAccessProviderImpl.requireOwnedTransaction(transactionId, securityUser.getId());
        transaction.setHide(!transaction.isHide());
        transactionRepository.save(transaction);

        bankAccountService.updateAccountBalance(transaction.getCard().getAccount(),
                !transaction.isHide() ? transaction.getAmount() : transaction.getAmount().negate());

    }

    @Override
    public void deleteTransaction(Long transactionId, SecurityUser securityUser) {
        Transaction transaction = entityAccessProviderImpl.requireOwnedTransaction(transactionId, securityUser.getId());
        transaction.setHide(!transaction.isHide());
        transactionRepository.delete(transaction);

        bankAccountService.updateAccountBalance(transaction.getCard().getAccount(),
                transaction.getAmount().negate());
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Override
    public Page<TransactionDto> getListTransaction(
            SecurityUser securityUser, LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size) {

        Specification<Transaction> spec = buildSpecification(
                securityUser.getId(), startDate, endDate, cardId, bankId, categoryId, description);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return transactions.map(transactionMapper::toTransactionDto);
    }

    @Override
    public void splitTransaction(Long transactionId, List<SubTransactionDto> subTransactions, SecurityUser securityUser) {
        Transaction parentTransaction = entityAccessProviderImpl.requireOwnedTransaction(transactionId, securityUser.getId());

        BigDecimal totalSubAmount = subTransactions.stream()
                .map(SubTransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (!totalSubAmount.equals(parentTransaction.getAmount())) {
            throw new IllegalArgumentException("Сумма дочерних транзакций не равна сумме родительской");
        }

        parentTransaction.setHide(true);
        parentTransaction.setMaster(true);
        transactionRepository.save(parentTransaction);

        for (SubTransactionDto subTransactionDto : subTransactions) {
            if (subTransactionDto.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                Transaction subTransaction = getSubTransaction(subTransactionDto, parentTransaction);
                transactionRepository.save(subTransaction);
            }
        }
    }

    @Override
    public List<TransactionDto> insertTransaction(NewTransactionDto newTransactionDto, SecurityUser securityUser) {
        List<TransactionDto> transactionDtoList = new ArrayList<>();
        Transaction revTransaction = null;
        User user = userService.getUserById(securityUser.getId());

        Transaction transaction = prepareNewTransaction(newTransactionDto, user , newTransactionDto.getCardId());
        if (newTransactionDto.getRevCardId() != null) {
            revTransaction = prepareNewTransaction(newTransactionDto, user, newTransactionDto.getRevCardId());
            revTransaction.setAmount(revTransaction.getAmount().negate());
        }
        if (revTransaction != null) {
            if (!Objects.equals(revTransaction.getCurrency().getId(), transaction.getCurrency().getId())) {
                throw new IllegalArgumentException("Валюты карт не совпадают");
            }
            transactionDtoList.add(transactionMapper.toTransactionDto(saveNewTransaction(revTransaction)));
        }
        transactionDtoList.add(transactionMapper.toTransactionDto(saveNewTransaction(transaction)));
        return transactionDtoList;

    }

    private Transaction saveNewTransaction(Transaction transaction) {
        bankAccountService.updateAccountBalance(transaction.getCard().getAccount(),
                transaction.getAmount());
        return transactionRepository.save(transaction);
    }


    private Transaction prepareNewTransaction(NewTransactionDto newTransactionDto,User user, @NotNull(message = "{validation.Transaction.cardId.NotNull}") Long cardId) {
        Transaction transaction = new Transaction();
        transaction.setDescription(newTransactionDto.getDescription());
        transaction.setAmount(newTransactionDto.getAmount());
        transaction.setOperationTime(newTransactionDto.getOperationTime());
        transaction.setCategory(entityAccessProviderImpl.requireOwnedCategory(
                newTransactionDto.getCategoryId(),
                user.getId()));
        transaction.setCard(entityAccessProviderImpl.requireOwnedCard(cardId, user.getId()));
        transaction.setCurrency(entityAccessProviderImpl.requireCurrencyByCardId(transaction.getCard().getId()));
        transaction.setOperationType(newTransactionDto.getOperationType());
        if (transaction.getOperationType() == OperationType.OUTGOING) {
            transaction.setAmount(transaction.getAmount().negate());
        }
        transaction.setUser(user);

        return transaction;

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


    @SuppressWarnings("checkStyle")
    private Specification<Transaction> buildSpecification(Long userId,
                                                          LocalDateTime startDate,
                                                          LocalDateTime endDate,
                                                          Long cardId,
                                                          Long bankId,
                                                          Long categoryId,
                                                          String description) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operationTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operationTime"), endDate));
            }
            if (cardId != null) {
                predicates.add(criteriaBuilder.equal(root.get("card").get("id"), cardId));
            }
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (bankId != null) {
                predicates.add(criteriaBuilder.equal(root.get("card").get("account").get("bank").get("id"), bankId));
            }
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


}
