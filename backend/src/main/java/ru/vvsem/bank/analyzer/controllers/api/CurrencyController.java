package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.dto.CurrencyDto;
import ru.vvsem.bank.analyzer.services.CurrencyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/currency")
@Slf4j
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyDto>> getAll(Pageable pageable) {
        try {
            log.info("GET /api/currency");
            List<CurrencyDto> currencyDtoList = currencyService.getAllCurrencies();
            return ResponseEntity.ok(currencyDtoList);
        } catch (Exception e) {
            log.error("Error getting currencies");
            return ResponseEntity.internalServerError().build();
        }
    }
}
