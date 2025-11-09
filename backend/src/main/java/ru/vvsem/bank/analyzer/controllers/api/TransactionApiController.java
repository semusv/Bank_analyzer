package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.PageResponseDto;
import ru.vvsem.bank.analyzer.dto.transaction.NewTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.PatchTransactionData;
import ru.vvsem.bank.analyzer.dto.transaction.SubTransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionDto;
import ru.vvsem.bank.analyzer.dto.transaction.TransactionFilterDto;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.services.transaction.TransactionProcessingService;
import ru.vvsem.bank.analyzer.services.transaction.TransactionSearchService;
import ru.vvsem.bank.analyzer.services.transaction.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionApiController {

    private final TransactionService transactionService;

    private final TransactionSearchService transactionSearchService;

    private final TransactionProcessingService transactionProcessingService;

    @SuppressWarnings("checkstyle:ParameterNumber")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponseDto<TransactionDto> getListTransactions(
            @ModelAttribute TransactionFilterDto filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal SecurityUser securityUser) {


        Page<TransactionDto> result = transactionSearchService.searchTransactions(
                securityUser,
                filter,
                page,
                size);

        return PageResponseDto.from(result);
    }

    @RequestMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto getTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return transactionService.getUserTransaction(transactionId, securityUser.getId());
    }

    @RequestMapping("/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public TransactionDto hideTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return transactionProcessingService.hideTransactionWithBalanceUpdate(transactionId, securityUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(
            @PathVariable("id") Long transactionId,
            @AuthenticationPrincipal SecurityUser securityUser) {
        transactionProcessingService.deleteTransactionWithBalanceUpdate(transactionId, securityUser);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionDto patchTransaction(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody PatchTransactionData patchTransactionData,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return transactionProcessingService.patchTransactionWithBalanceUpdate(
                transactionId,
                patchTransactionData,
                securityUser);
    }

    @PostMapping("/{id}/split")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void splitTransaction(
            @PathVariable("id") Long transactionId,
            @Valid @RequestBody List<SubTransactionDto> subTransactions,
            @AuthenticationPrincipal SecurityUser securityUser) {
        transactionProcessingService.splitTransaction(transactionId, subTransactions, securityUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<TransactionDto> createTransaction(
            @Valid @RequestBody NewTransactionDto newTransactionDto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return transactionProcessingService.createTransaction(newTransactionDto, securityUser);
    }


}
