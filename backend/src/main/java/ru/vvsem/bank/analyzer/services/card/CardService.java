package ru.vvsem.bank.analyzer.services.card;

import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;

import java.util.List;

public interface CardService {

    CardDto createCard(NewCardDto newCardDto, SecurityUser securityUser);

    void deleteCard(Long cardId, SecurityUser securityUser);

    List<CardDto> getCardListDto(SecurityUser securityUser);

    CardDto getCardDtoById(Long cardId, SecurityUser securityUser);
}
