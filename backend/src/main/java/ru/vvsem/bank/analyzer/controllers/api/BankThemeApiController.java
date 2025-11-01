package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.BankThemeDto;
import ru.vvsem.bank.analyzer.services.BankThemeService;

@RestController
@RequestMapping("/api/bank-themes")
@RequiredArgsConstructor
public class BankThemeApiController {

    private final BankThemeService bankThemeService;

    @GetMapping("/{bankCode}")
    @ResponseStatus(HttpStatus.OK)
    public BankThemeDto getTheme(@PathVariable String bankCode) {
        return bankThemeService.getBankTheme(bankCode);
    }
}
