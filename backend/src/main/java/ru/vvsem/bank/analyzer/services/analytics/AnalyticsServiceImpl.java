package ru.vvsem.bank.analyzer.services.analytics;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.category.CategoryDto;
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.Transaction;
import ru.vvsem.bank.analyzer.models.enums.OperationType;
import ru.vvsem.bank.analyzer.repositories.TransactionRepository;
import ru.vvsem.bank.analyzer.services.card.CardService;
import ru.vvsem.bank.analyzer.services.category.CategoryService;
import ru.vvsem.bank.analyzer.services.exchange_rate.ExchangeRateService;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;

    private final CardService cardService;

    private final CategoryService categoryService;

    private final ExchangeRateService exchangeRateService;

    private final CustomUserDetailsService userService;

    @Override
    public List<TimeSeriesDto> getTimeSeries(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            List<Long> cardIdList,
            List<OperationType> operationTypeList,
            SecurityUser securityUser) {
        Map<LocalDate, BigDecimal> mapTimeseries = new HashMap<>();
        Specification<Transaction> spec = buildSpecification(
                securityUser.getId(), startDateTime, endDateTime, cardIdList, operationTypeList);
        List<Transaction> transactions = transactionRepository.findAll(spec);

        for (Transaction transaction : transactions) {
            LocalDate operationDate = transaction.getOperationTime().toLocalDate();
            BigDecimal currentAmount = mapTimeseries.getOrDefault(operationDate, BigDecimal.ZERO);
            mapTimeseries.put(operationDate, currentAmount.add(exchangeRateService.convertToRub(
                    transaction.getAmount(),
                    transaction.getCurrency().getCode())));
        }
        for (int i = 0; i <= ChronoUnit.DAYS.between(
                startDateTime.toLocalDate().atStartOfDay(),
                endDateTime.toLocalDate().atStartOfDay()); i++) {
            if (!mapTimeseries.containsKey(startDateTime.toLocalDate().plusDays(i))) {
                mapTimeseries.put(startDateTime.toLocalDate().plusDays(i), BigDecimal.ZERO);
            }
        }

        // Преобразуем в список DTO
        return mapTimeseries.entrySet().stream()
                .map(entry -> new TimeSeriesDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TimeSeriesDto::getDate))
                .toList();
    }

    @Override
    public List<CategoryBreakdownDto> getCategoryBreakdown(LocalDateTime startDateTime,
                                                           LocalDateTime endDateTime,
                                                           List<Long> cardIdList,
                                                           List<OperationType> operationTypeList,
                                                           SecurityUser securityUser) {
        Map<Long, BigDecimal> countedCategoriesMap = new HashMap<>();
        Map<Long, CategoryDto> categoryMap = new HashMap<>();
        categoryService.getCategoriesForUser(securityUser)
                .forEach(category -> categoryMap.put(category.getId(), category));

        Specification<Transaction> spec = buildSpecification(
                securityUser.getId(), startDateTime, endDateTime, cardIdList, operationTypeList);
        List<Transaction> transactions = transactionRepository.findAll(spec);

        for (Transaction transaction : transactions) {
            Long operationId = transaction.getCategory().getId();
            BigDecimal currentAmount = countedCategoriesMap.getOrDefault(operationId, BigDecimal.ZERO);
            countedCategoriesMap.put(
                    operationId,
                    currentAmount.add(exchangeRateService.convertToRub(
                            transaction.getAmount(),
                            transaction.getCurrency().getCode())));
        }

        return countedCategoriesMap.entrySet().stream()
                .map(entry ->
                        new CategoryBreakdownDto(
                                categoryMap.get(entry.getKey()),
                                entry.getValue().abs()))
                .sorted(Comparator.comparing(CategoryBreakdownDto::getAmount))
                .toList();
    }

    @SuppressWarnings("CheckStyle")
    private Specification<Transaction> buildSpecification(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<Long> cardIdList,
            List<OperationType> operationTypeList) { // Изменено: Long → OperationType

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // User
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));
            // Operation time
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("operationTime"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("operationTime"), endDate));
            }
            // Card filter
            if (cardIdList != null && !cardIdList.isEmpty()) {
                predicates.add(root.get("card").get("id").in(cardIdList));
            }
            // Operation type filter (новый код)
            if (operationTypeList != null && !operationTypeList.isEmpty()) {
                predicates.add(root.get("operationType").in(operationTypeList));
            }
            predicates.add(criteriaBuilder.equal(root.get("hide"), false));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
