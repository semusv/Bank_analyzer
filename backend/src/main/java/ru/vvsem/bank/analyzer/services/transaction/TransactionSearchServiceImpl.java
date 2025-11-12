package ru.vvsem.bank.analyzer.services.transaction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDtoWithSiblings;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionFilterDto;
import ru.vvsem.bank.analyzer.mappers.transaction.TransactionMapper;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TransactionSearchServiceImpl implements TransactionSearchService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final TransactionServiceImpl transactionService;

    @Override
    public Page<TransactionDtoWithSiblings> searchTransactions(
            SecurityUser securityUser,
            TransactionFilterDto filter,
            int page,
            int size) {

        Specification<Transaction> spec = buildSpecification(
                securityUser.getId(), filter);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
        return transactionService.searchTransactions(spec, pageable);
    }

    private Specification<Transaction> buildSpecification(Long userId, TransactionFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addUserIdPredicate(predicates, root, criteriaBuilder, userId);
            addStartDateTimePredicate(predicates, root, criteriaBuilder, filter);
            addEndDateTimePredicate(predicates, root, criteriaBuilder, filter);
            addCardIdPredicate(predicates, root, criteriaBuilder, filter);
            addCategoryIdPredicate(predicates, root, criteriaBuilder, filter);
            addBankIdPredicate(predicates, root, criteriaBuilder, filter);
            addDescriptionPredicate(predicates, root, criteriaBuilder, filter);
            addParentTransactionPredicate(predicates, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserIdPredicate(List<Predicate> predicates, Root<Transaction> root,
                                    CriteriaBuilder cb, Long userId) {
        predicates.add(cb.equal(root.get("user").get("id"), userId));
    }

    private void addStartDateTimePredicate(List<Predicate> predicates, Root<Transaction> root,
                                           CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getStartDateTime() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), filter.getStartDateTime()));
        }
    }

    private void addEndDateTimePredicate(List<Predicate> predicates, Root<Transaction> root,
                                         CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getEndDateTime() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), filter.getEndDateTime()));
        }
    }

    private void addCardIdPredicate(List<Predicate> predicates, Root<Transaction> root,
                                    CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getCardId() != null) {
            predicates.add(cb.equal(root.get("card").get("id"), filter.getCardId()));
        }
    }

    private void addCategoryIdPredicate(List<Predicate> predicates, Root<Transaction> root,
                                        CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getCategoryId() != null) {
            predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
        }
    }

    private void addBankIdPredicate(List<Predicate> predicates, Root<Transaction> root,
                                    CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getBankId() != null) {
            predicates.add(cb.equal(root.get("card").get("account").get("bank").get("id"), filter.getBankId()));
        }
    }

    private void addDescriptionPredicate(List<Predicate> predicates, Root<Transaction> root,
                                         CriteriaBuilder cb, TransactionFilterDto filter) {
        if (filter.getDescription() != null && !filter.getDescription().trim().isEmpty()) {
            predicates.add(cb.like(
                    cb.lower(root.get("description")),
                    "%" + filter.getDescription().toLowerCase() + "%"
            ));
        }
    }

    private void addParentTransactionPredicate(List<Predicate> predicates, Root<Transaction> root,
                                               CriteriaBuilder cb) {
        predicates.add(cb.isNull(root.get("parentTransaction")));
    }


}
