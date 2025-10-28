package ru.vvsem.bank.analyzer.services;

import jakarta.persistence.criteria.Predicate;
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
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.Category;
import ru.vvsem.bank.analyzer.models.Currency;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.repositories.CategoryRepository;
import ru.vvsem.bank.analyzer.repositories.CurrencyRepository;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final CardRepository cardRepository;

    private final CurrencyRepository currencyRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public List<TransactionDto> getListTransaction(Long userId) {
        return transactionRepository.findByUserId(userId)
                .stream()
                .map(transactionMapper::toTransactionDto)
                .toList();
    }

    @Override
    public TransactionDto getTransaction(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUser_Id(transactionId, userId)
                .map(transactionMapper::toTransactionDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaction with id %d for UserId %d not found".formatted(transactionId, userId),
                        "exception.entity.not.found.transaction"));
    }

    @Override
    public void hideTransaction(Long transactionId, Long userId) {
        Transaction transaction = getTransactionWithIdAndUserId(transactionId, userId);
        transaction.setHide(!transaction.isHide());
        transactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = getTransactionWithIdAndUserId(transactionId, userId);
        transaction.setHide(!transaction.isHide());
        transactionRepository.delete(transaction);
    }

    @Override
    public Page<TransactionDto> getListTransaction(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description,
            int page,
            int size) {

        Specification<Transaction> spec = buildSpecification(
                userId, startDate, endDate, cardId, bankId, categoryId, description);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        Page<Transaction> transactions = transactionRepository.findAll(spec, pageable);
        return transactions.map(transactionMapper::toTransactionDto);
    }

    @Override
    public void splitTransaction(Long transactionId, List<SubTransactionDto> subTransactions, Long userId) {
        Transaction parentTransaction = getTransactionWithIdAndUserId(transactionId, userId);

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
    public TransactionDto insertTransaction(NewTransactionDto newTransactionDto, User user) {

        Transaction transaction = new Transaction();
        transaction.setDescription(newTransactionDto.getDescription());
        transaction.setAmount(newTransactionDto.getAmount());
        transaction.setOperationTime(newTransactionDto.getOperationTime());
        transaction.setCategory(getCategoryById(newTransactionDto.getCategoryId()));
        transaction.setCurrency(getCurrencyByCardID(newTransactionDto.getCardId()));
        transaction.setCard(getCardByIdAndUserId(newTransactionDto.getCardId(), user.getId()));
        transaction.setOperationType(newTransactionDto.getOperationType());
        if (transaction.getOperationType() == OperationType.OUTGOING) {
            transaction.setAmount(transaction.getAmount().negate());
        }
        transaction.setUser(user);

        return transactionMapper.toTransactionDto(
                transactionRepository.save(transaction));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(categoryId),
                        "exception.entity.not.found.category"));
    }

    private Currency getCurrencyByCardID(Long cardId) {
        return currencyRepository.getCurrencyByCardId(cardId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Currency for card with id %d not found".formatted(cardId),
                        "exception.entity.not.found.currency"));
    }

    private Card getCardByIdAndUserId(Long cardId, Long userId) {
        return cardRepository.findByIdAndAccount_User_Id(cardId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Card with id %d for UserId %d not found".formatted(cardId, userId),
                        "exception.entity.not.found.card"));
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


    private Specification<Transaction> buildSpecification(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Long cardId,
            Long bankId,
            Long categoryId,
            String description) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Базовый фильтр по пользователю
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            // Фильтр по дате
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("operationTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("operationTime"), endDate));
            }
            // Фильтр по карте
            if (cardId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("card").get("id"), cardId));
            }

            // Фильтр по категории
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("id"), categoryId));
            }

            // Фильтр по банку
            if (bankId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("card").get("account").get("bank").get("id"), bankId));
            }

            // Фильтр по описанию
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        "%" + description.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Transaction getTransactionWithIdAndUserId(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUser_Id(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Transaction with id %d for UserId %d not found".formatted(transactionId, userId),
                        "exception.entity.not.found.transaction"));
    }

}
