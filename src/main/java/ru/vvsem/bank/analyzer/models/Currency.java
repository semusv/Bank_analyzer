package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Entity
@Table(name = "currencies")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Currency extends AbstractBaseEntity {

    @NotNull
    @Column(name = "code", unique = true, length = 3)
    private String code; // "USD", "EUR", "RUB"

    @NotNull
    @Column(name = "symbol")
    private String symbol; // "$", "€", "₽"

    @NotNull
    @Column(name = "name")
    private String name; // "Доллар США", "Евро"
}
