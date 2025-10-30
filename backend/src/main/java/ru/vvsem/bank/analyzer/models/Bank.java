package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "banks")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Bank extends AbstractBaseEntity {

    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name; // "Тинькофф", "Сбербанк"

    @NotNull
    @Column(name = "bic", nullable = false, unique = true, length = 9)
    private String bic; // БИК банка

    @Column(name = "bank_code", length = 50)
    private String bankCode;

    @Column(name = "logo_url")
    private String logoUrl;

    @OneToMany(mappedBy = "bank")
    @ToString.Exclude
    private List<BankAccount> accounts;
}
