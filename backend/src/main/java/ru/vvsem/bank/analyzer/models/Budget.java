package ru.vvsem.bank.analyzer.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Builder
@Entity
@Table(name = "budgets")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Budget extends AbstractBaseEntity {

    @Column(name = "limit_amount", precision = 19, scale = 2)
    @NotNull
    private BigDecimal limitAmount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @NotNull
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User user;
}
