package ru.vvsem.bank.analyzer.services.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.mappers.CardMapper;
import ru.vvsem.bank.analyzer.models.Card;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.CardRepository;
import ru.vvsem.bank.analyzer.services.bank.BankService;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;

    private final CardMapper cardMapper;

    private final EntityAccessProvider entityAccessProvider;

    private final BankService bankService;

    @Override
    @Transactional
    public CardDto createCard(NewCardDto newCardDto, SecurityUser securityUser) {
        var card = cardMapper.toEntity(newCardDto);
        card.setAccount(entityAccessProvider.getOwnedBankAccount(
                card.getAccount().getId(), securityUser.getId()));
        card.setIssuerBank(bankService.findById(card.getAccount().getBank().getId()));
        var newCard = cardRepository.save(card);
        return cardMapper.toCardDto(newCard);
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId, SecurityUser securityUser) {
        Card card = entityAccessProvider.getOwnedCard(cardId, securityUser.getId());
        cardRepository.delete(card);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getCardListDto(SecurityUser securityUser) {
        return cardRepository.findByAccountUserId(securityUser.getId())
                .stream()
                .map(cardMapper::toCardDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardDtoById(Long cardId, SecurityUser securityUser) {
        return cardMapper.toCardDto(
                entityAccessProvider.getOwnedCard(cardId, securityUser.getId()));
    }


}
