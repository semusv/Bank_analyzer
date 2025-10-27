package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import org.hibernate.query.QueryParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.TransactionDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.TransactionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @RequestMapping
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionDto> getListTransactions(
            @RequestParam(required = false, name = "startDate") LocalDate startDate,
            @RequestParam(required = false, name = "endDate") LocalDate endDate,
            @RequestParam(required = false, name = "cardId") Long cardId,
            @RequestParam(required = false, name = "bankId") Long bankId,
            @RequestParam(required = false, name = "categoryId") Long categoryId,
            @RequestParam(required = false, name = "description") String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String[] sort,
            @AuthenticationPrincipal User user
    ) {
        return transactionService.getListTransaction(
                user.getId(),
                startDate.atStartOfDay(),
                endDate.atStartOfDay(),
                cardId,
                bankId,
                categoryId,
                description,
                page,
                size,
                sort
        );
    }

    @RequestMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto getTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal User user) {
        return transactionService.getTransaction(transactionId, user.getId());
    }

    @RequestMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal User user) {
        transactionService.hideTransaction(transactionId, user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal User user) {
        transactionService.deleteTransaction(transactionId, user.getId());
    }

}
