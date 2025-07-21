package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "description")
    @NotNull
    private String description; // "Пятерочка Магнит"

    @Column(name = "amount", precision = 19, scale = 2)
    @NotNull
    private BigDecimal amount; // 100.00

    @Column(name = "operation_time")
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime operationTime;

    @Column(name = "hide", nullable = false)
    private boolean hide; // Исключить из аналитики

    @Column(name = "master", nullable = false)
    private boolean master; //главная транзакция или подтранзакция

    // Связи
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_id")
    @NotNull
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

    @OneToMany(mappedBy = "parentTransaction", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Transaction> subTransactions;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;

    // Валидация суммы подтранзакций
    @AssertTrue
    public boolean isSubTransactionsSumValid() {
        if (subTransactions.isEmpty()) {
            return true;
        }

        BigDecimal sum = subTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.compareTo(amount) == 0;
    }

}
