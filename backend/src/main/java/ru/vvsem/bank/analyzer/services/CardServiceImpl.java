package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.CardMapper;
import ru.vvsem.bank.analyzer.mappers.UserMapper;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.BankAccount;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.repositories.BankAccountRepository;
import ru.vvsem.bank.analyzer.repositories.BankRepository;
import ru.vvsem.bank.analyzer.repositories.CardRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    private final UserService userService;

    private final UserMapper userMapper;

    private final CardMapper cardMapper;

    private final BankAccountRepository bankAccountRepository;

    private final BankRepository bankRepository;

    @Override
    @Transactional
    public CardDto createCard(NewCardDto newCardDto, User user) {
        log.info("Creating new card for user: {}", user.getId());
        var card = cardMapper.toEntity(newCardDto);
        card.setAccount(prepareAccount(card.getAccount().getId()));
        card.setIssuerBank(prepareIssuerBank(card.getAccount().getBank().getId()));
        var newCard = cardRepository.save(card);
        return cardMapper.toCardDto(newCard);
    }

    @Override
    public void deleteCard(Long cardId, Long userId) {
        log.info("Deleting card: {} for user: {}", cardId, userId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "CardId %d for UserId %d not found".formatted(cardId, userId),
                                        "exception.entity.not.found.card")
                );

        cardRepository.delete(card);
    }

    private Bank prepareIssuerBank(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "Bank with id %d not found".formatted(id),
                                        "exception.entity.not.found.bank",
                                        id)
                );
    }

    private BankAccount prepareAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "BankAccount with id %d not found".formatted(id),
                                        "exception.entity.not.found.bankAccount",
                                        id)
                );
    }

}
