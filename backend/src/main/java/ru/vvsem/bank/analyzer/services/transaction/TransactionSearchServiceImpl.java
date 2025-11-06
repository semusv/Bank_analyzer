package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.mappers.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionSearchServiceImpl implements TransactionSearchService {
    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final TransactionServiceImpl transactionService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    @Override
    public Page<TransactionDto> searchTransactions(
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
        return transactionService.searchTransactions(spec, pageable);
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
