package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class ExchangeRate extends AbstractBaseEntity{
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "currency_date", nullable = false)
    private LocalDate currencyDate;

    @Column(name = "nominal", nullable = false)
    private Integer nominal;

    @Column(name = "value", precision = 10, scale = 6 , nullable = false)
    private BigDecimal value;

    @Column(name = "vunit_rate", precision = 10, scale = 6 , nullable = false)
    private BigDecimal vunitRate;

    @Column(name = "currency_name", length = 100 , nullable = false)
    private String currencyName;
}

