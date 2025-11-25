package ru.vvsem.bank.analyzer.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.vvsem.bank.analyzer.models.Card;

/**
 * DTO for {@link Card}
 */
@Getter
@Setter
public class NewCardDto {

    @NotBlank(message = "{validation.Card.lastFourDigits.notBlank}")
    @Size(min = 4, max = 4, message = "{validation.Card.lastFourDigits.size}")
    private String lastFourDigits;

    @NotBlank(message = "{validation.Card.lastFourDigits.cardName}")
    private String cardName;

    @NotNull(message = "{validation.Card.accountId.notNull}")
    private Long accountId;

}
