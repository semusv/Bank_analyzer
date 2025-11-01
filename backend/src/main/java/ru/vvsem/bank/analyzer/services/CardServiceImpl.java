package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.mappers.CardMapper;
import ru.vvsem.bank.analyzer.mappers.UserMapper;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.services.security.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    private final UserService userService;

    private final UserMapper userMapper;

    private final CardMapper cardMapper;

    private final EntityAccessProvider entityAccessProvider;

    @Override
    @Transactional
    public CardDto createCard(NewCardDto newCardDto, User user) {
        log.info("Creating new card for user: {}", user.getId());
        var card = cardMapper.toEntity(newCardDto);
        card.setAccount(entityAccessProvider.requireOwnedBankAccount(
                card.getAccount().getId(),user.getId()));
        card.setIssuerBank(entityAccessProvider.requireBank(card.getAccount().getBank().getId()));
        var newCard = cardRepository.save(card);
        return cardMapper.toCardDto(newCard);
    }

    @Override
    public void deleteCard(Long cardId, Long userId) {
        log.info("Deleting card: {} for user: {}", cardId, userId);
        Card card = entityAccessProvider.requireOwnedCard(cardId, userId);
        cardRepository.delete(card);
    }

    @Override
    public List<CardDto> getCardList(Long userId) {

        return cardRepository.findByAccountUserId(userId)
                .stream()
                .map(cardMapper::toCardDto)
                .toList();
    }


}
