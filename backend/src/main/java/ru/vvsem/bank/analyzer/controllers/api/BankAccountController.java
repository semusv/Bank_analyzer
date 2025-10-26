package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.Accounts.NewBankAccountDto;
import ru.vvsem.bank.analyzer.dto.Accounts.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.BankAccountService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bankAccount")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @GetMapping
    //TODO: перенести статус в аннотацию и сделать обработке ошибок
    public ResponseEntity<List<BankAccountSimpleDto>> getUserAccounts(@AuthenticationPrincipal User user) {
        try {
            log.info("GET /api/accounts for user: {}", user.getUsername());
            List<BankAccountSimpleDto> accounts = bankAccountService.getUserAccountsWithCards(user.getId());
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            log.error("Error getting accounts for user: {}", user.getUsername(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountSimpleDto createAccount(
            @Valid @RequestBody NewBankAccountDto newBankAccountDto,
            @AuthenticationPrincipal User user
    ) {
        return bankAccountService.createAccount(newBankAccountDto, user);
    }


}
