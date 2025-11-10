package ru.vvsem.bank.analyzer.services.card;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface CardService {

    @Transactional
    CardDto createCard(NewCardDto newCardDto, SecurityUser securityUser);

    @Transactional
    void deleteCard(Long cardId, SecurityUser securityUser);

    @Transactional(readOnly = true)
    List<CardDto> getCardList(SecurityUser securityUser);
}
