package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.vvsem.bank.analyzer.models.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Builder
@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends AbstractBaseEntity {

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;// "Пятерочка Магнит"

    @NotNull
    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount; // 100.00

    @NotNull
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;

    @Column(name = "hide", nullable = false)
    private boolean hide = false; // Исключить из аналитики

    @Column(name = "master", nullable = false)
    private boolean master = false;  //главная транзакция или подтранзакция

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType = OperationType.CARD;

    /* FK‑связи */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_transaction_id")
    private Transaction parentTransaction; // Для подтранзакций

    @OneToMany(mappedBy = "parentTransaction", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Transaction> subTransactions = new ArrayList<>();

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Валидация суммы подтранзакций
    @AssertTrue(message = "Сумма дочерних транзакций не совпадает с суммой родительской")
    public boolean isSubTransactionsSumValid() {
        if (subTransactions == null || subTransactions.isEmpty()) {
            return true;
        }

        BigDecimal sum = subTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.compareTo(amount) == 0;
    }

}
