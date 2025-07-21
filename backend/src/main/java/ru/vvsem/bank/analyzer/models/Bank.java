package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Builder
@Entity
@Table(name = "banks")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Bank extends AbstractBaseEntity {

    @NotNull
    @Column(name = "name" , unique = true)
    private String name; // "Тинькофф", "Сбербанк"

    @NotNull
    @Column(name = "bic", unique = true, length = 9)
    private String bic; // БИК банка

    @Column(name = "logoUrl")
    private String logoUrl; // Ссылка на логотип

    @OneToMany(mappedBy = "bank")
    @ToString.Exclude
    private List<BankAccount> accounts;
}
