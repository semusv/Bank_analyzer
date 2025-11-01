package ru.vvsem.bank.analyzer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cards")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Card extends AbstractBaseEntity {

    @NotNull
    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;// "1234"

    @NotNull
    @Column(name = "card_name", nullable = false)
    private String cardName; // "Tinkoff Black"

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private BankAccount account;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_bank_id", nullable = false)
    private Bank issuerBank; // Банк-эмитент карты (может отличаться от банка счета)
}
