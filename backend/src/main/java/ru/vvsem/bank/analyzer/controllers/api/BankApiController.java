package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.services.bank.BankService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankApiController {

    private final BankService banksService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BankDto> getBanks() {
        return banksService.getBankDtos();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BankDto getBankById(
            @PathVariable("id") Long bankId) {
        return banksService.findBankDtoById(bankId);
    }

}
