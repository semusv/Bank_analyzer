package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vvsem.bank.analyzer.dto.card.CardDto;
import ru.vvsem.bank.analyzer.dto.card.NewCardDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.card.CardService;

import java.util.List;

@RestController
@RequestMapping("/api/card")
@RequiredArgsConstructor
public class CardApiController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto createCard(
            @Valid @RequestBody NewCardDto newCardDto,
            @AuthenticationPrincipal User user
    ) {
        return cardService.createCard(newCardDto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user
    ) {
        cardService.deleteCard(id, user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CardDto> getCardList(
            @AuthenticationPrincipal User user
    ) {
        return cardService.getCardList(user);
    }
}

