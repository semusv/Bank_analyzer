package ru.vvsem.bank.analyzer.services.card;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.User;

import java.util.List;

public interface CardService {

    @Transactional
    CardDto createCard(NewCardDto newCardDto, User user);

    @Transactional
    void deleteCard(Long cardId, User user);

    @Transactional
    List<CardDto> getCardList(User user);
}
