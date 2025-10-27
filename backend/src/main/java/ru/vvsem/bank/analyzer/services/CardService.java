package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.User;

public interface CardService {

    @Transactional
    CardDto createCard(NewCardDto newCardDto, User user);

    @Transactional
    void deleteCard(Long cardId, Long userId);
}
