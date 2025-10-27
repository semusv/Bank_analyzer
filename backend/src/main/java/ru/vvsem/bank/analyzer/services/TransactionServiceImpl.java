package ru.vvsem.bank.analyzer.services;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.TransactionDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

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
