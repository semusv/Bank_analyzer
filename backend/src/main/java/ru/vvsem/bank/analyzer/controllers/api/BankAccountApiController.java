package ru.vvsem.bank.analyzer.controllers.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.account.NewBankAccountDto;
import ru.vvsem.bank.analyzer.dto.account.BankAccountSimpleDto;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.BankAccountService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bankAccount")
@RequiredArgsConstructor
public class BankAccountApiController {
    private final BankAccountService bankAccountService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BankAccountSimpleDto> getUserAccounts(@AuthenticationPrincipal User user) {
        log.info("GET /api/accounts for user: {}", user.getUsername());
        return bankAccountService.getUserAccountsWithCards(user.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BankAccountSimpleDto createAccount(
            @Valid @RequestBody NewBankAccountDto newBankAccountDto,
            @AuthenticationPrincipal User user
    ) {
        return bankAccountService.createAccount(newBankAccountDto, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBankAccont(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User user
    ) {
        bankAccountService.deleteAccount(id, user.getId());
    }


}
