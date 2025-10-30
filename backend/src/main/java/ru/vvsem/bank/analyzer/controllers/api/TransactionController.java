package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
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

    @SuppressWarnings("CheckStyle")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<TransactionDto> getListTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user) {

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Page<TransactionDto> result = transactionService.getListTransaction(
                user.getId(),
                startDateTime,
                endDateTime,
                cardId,
                bankId,
                categoryId,
                description,
                page,
                size);

        return PageResponse.from(result);
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

    @PostMapping("/{id}/split")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void splitTransaction(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody List<SubTransactionDto> subTransactions,
            @AuthenticationPrincipal User user) {
        transactionService.splitTransaction(
                transactionId,
                subTransactions,
                user.getId());

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDto createTransaction(
            @Valid @RequestBody NewTransactionDto newTransactionDto,
            @AuthenticationPrincipal User user) {
       return transactionService.insertTransaction(newTransactionDto, user);
    }

    @Setter
    @Getter
    public static class PageResponse<T> {
        // Getters and Setters
        private java.util.List<T> content;

        private long totalElements;

        private int totalPages;

        private int pageNumber;

        private int pageSize;

        private boolean first;

        private boolean last;

        public PageResponse() {
        }

        @SuppressWarnings("CheckStyle")
        public PageResponse(java.util.List<T> content, long totalElements, int totalPages,
                            int pageNumber, int pageSize, boolean first, boolean last) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.first = first;
            this.last = last;
        }

        public static <T> PageResponse<T> from(Page<T> page) {
            return new PageResponse<>(
                    page.getContent(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getNumber(),
                    page.getSize(),
                    page.isFirst(),
                    page.isLast());
        }

    }
}
