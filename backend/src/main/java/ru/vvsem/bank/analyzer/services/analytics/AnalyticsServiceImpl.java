package ru.vvsem.bank.analyzer.services.analytics;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.analytics.SeriesFilterDto;
import ru.vvsem.bank.analyzer.dto.analytics.CategoryBreakdownDto;
import ru.vvsem.bank.analyzer.dto.analytics.TimeSeriesDto;
import ru.vvsem.bank.analyzer.mappers.CategoryMapper;
import ru.vvsem.bank.analyzer.mappers.OperationTypeMapper;
import ru.vvsem.bank.analyzer.models.Category;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionRepository transactionRepository;

    private final CardService cardService;

    private final CategoryService categoryService;

    private final ExchangeRateService exchangeRateService;

    private final CustomUserDetailsService userService;

    private final OperationTypeMapper operationTypeMapper;

    private final CategoryMapper categoryMapper;

    @Override
    public List<TimeSeriesDto> getTimeSeries(
            SeriesFilterDto filter,
            SecurityUser securityUser) {
        Specification<Transaction> spec = buildSpecification(securityUser.getId(), filter);
        List<Transaction> transactions = transactionRepository.findAll(spec);
        // Суммируем по датам
        Map<LocalDate, BigDecimal> aggregatedData = aggregateTransactionsByDate(transactions);
        // Дополняем пустыми датами
        Map<LocalDate, BigDecimal> filledData = fillMissingDates(aggregatedData, filter);
        // Преобразуем в список DTO
        return convertToTimeSeriesDto(filledData);
    }

    private Map<LocalDate, BigDecimal> aggregateTransactionsByDate(List<Transaction> transactions) {
        Map<LocalDate, BigDecimal> dateAmountMap = new HashMap<>();

        for (Transaction transaction : transactions) {
            LocalDate operationDate = transaction.getOperationTime().toLocalDate();
            BigDecimal amountInRub = exchangeRateService.convertToRub(
                    transaction.getAmount(),
                    transaction.getCurrency().getCode()
            );

            BigDecimal currentAmount = dateAmountMap.getOrDefault(operationDate, BigDecimal.ZERO);
            dateAmountMap.put(operationDate, currentAmount.add(amountInRub));
        }

        return dateAmountMap;
    }

    private Map<LocalDate, BigDecimal> fillMissingDates(
            Map<LocalDate, BigDecimal> aggregatedData,
            SeriesFilterDto filter) {

        Map<LocalDate, BigDecimal> filledData = new HashMap<>(aggregatedData);
        long daysBetween = ChronoUnit.DAYS.between(filter.getStartDate(), filter.getEndDate());

        for (int i = 0; i <= daysBetween; i++) {
            LocalDate currentDate = filter.getStartDate().plusDays(i);
            filledData.putIfAbsent(currentDate, BigDecimal.ZERO);
        }

        return filledData;
    }

    private List<TimeSeriesDto> convertToTimeSeriesDto(Map<LocalDate, BigDecimal> data) {
        return data.entrySet().stream()
                .map(entry -> new TimeSeriesDto(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TimeSeriesDto::getDate))
                .toList();
    }

    public List<CategoryBreakdownDto> getCategoryBreakdown(SeriesFilterDto filter,
                                                           SecurityUser securityUser) {
        // Получаем категории пользователя
        Map<Long, Category> categoryMap = getCategoriesMap(securityUser);

        // Получаем транзакции по фильтру
        List<Transaction> transactions = getFilteredTransactions(filter, securityUser);

        // Агрегируем суммы по категориям
        Map<Long, BigDecimal> categoryAmounts = aggregateAmountsByCategory(transactions);

        // Преобразуем в DTO и сортируем
        return convertToCategoryBreakdownDto(categoryAmounts, categoryMap);
    }

    private Map<Long, Category> getCategoriesMap(SecurityUser securityUser) {
        return categoryService.getCategoryEntitiesForUser(securityUser).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    private List<Transaction> getFilteredTransactions(SeriesFilterDto filter, SecurityUser securityUser) {
        Specification<Transaction> spec = buildSpecification(securityUser.getId(), filter);
        return transactionRepository.findAll(spec);
    }

    private Map<Long, BigDecimal> aggregateAmountsByCategory(List<Transaction> transactions) {
        Map<Long, BigDecimal> categoryAmounts = new HashMap<>();

        for (Transaction transaction : transactions) {
            Long categoryId = transaction.getCategory().getId();
            BigDecimal amountInRub = exchangeRateService.convertToRub(
                    transaction.getAmount(),
                    transaction.getCurrency().getCode()
            );
            BigDecimal currentAmount = categoryAmounts.getOrDefault(categoryId, BigDecimal.ZERO);
            categoryAmounts.put(categoryId, currentAmount.add(amountInRub));
        }
        return categoryAmounts;
    }

    private List<CategoryBreakdownDto> convertToCategoryBreakdownDto(
            Map<Long, BigDecimal> categoryAmounts,
            Map<Long, Category> categoryMap) {

        return categoryAmounts.entrySet().stream()
                .map(entry -> createCategoryBreakdownDto(entry, categoryMap))
                .sorted(Comparator.comparing(CategoryBreakdownDto::getAmount))
                .toList();
    }

    private CategoryBreakdownDto createCategoryBreakdownDto(
            Map.Entry<Long, BigDecimal> entry,
            Map<Long, Category> categoryMap) {

        Category category = categoryMap.get(entry.getKey());
        BigDecimal amount = entry.getValue().abs(); // Используем абсолютное значение

        return new CategoryBreakdownDto(categoryMapper.toCategoryDto(category), amount);
    }

    private Specification<Transaction> buildSpecification(Long userId, SeriesFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            addUserIdPredicate(predicates, root, criteriaBuilder, userId);
            addStartDateTimePredicate(predicates, root, criteriaBuilder, filter);
            addEndDateTimePredicate(predicates, root, criteriaBuilder, filter);
            addCardIdListPredicate(predicates, root,  filter);
            addOperationTypePredicate(predicates, root, filter);
            addHidePredicate(predicates, root, criteriaBuilder);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addUserIdPredicate(List<Predicate> predicates, Root<Transaction> root,
                                    CriteriaBuilder cb, Long userId) {
        predicates.add(cb.equal(root.get("user").get("id"), userId));
    }

    private void addStartDateTimePredicate(List<Predicate> predicates, Root<Transaction> root,
                                           CriteriaBuilder cb, SeriesFilterDto filter) {
        if (filter.getStartDateTime() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), filter.getStartDateTime()));
        }
    }

    private void addEndDateTimePredicate(List<Predicate> predicates, Root<Transaction> root,
                                         CriteriaBuilder cb, SeriesFilterDto filter) {
        if (filter.getEndDateTime() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), filter.getEndDateTime()));
        }
    }

    private void addCardIdListPredicate(List<Predicate> predicates, Root<Transaction> root, SeriesFilterDto filter) {
        if (filter.getCardIdList() != null && !filter.getCardIdList().isEmpty()) {
            predicates.add(root.get("card").get("id").in(filter.getCardIdList()));
        }
    }

    private void addOperationTypePredicate(List<Predicate> predicates, Root<Transaction> root,
                                            SeriesFilterDto filter) {
        if (filter.getOperationTypeIdList() == null || filter.getOperationTypeIdList().isEmpty()) {
            return;
        }

        List<OperationType> operationTypeList = filter.getOperationTypeIdList().stream()
                .map(operationTypeMapper::mapOperationType)
                .filter(Objects::nonNull)
                .toList();

        if (!operationTypeList.isEmpty()) {
            predicates.add(root.get("operationType").in(operationTypeList));
        }
    }

    private void addHidePredicate(List<Predicate> predicates, Root<Transaction> root,
                                  CriteriaBuilder cb) {
        predicates.add(cb.equal(root.get("hide"), false));
    }

}
