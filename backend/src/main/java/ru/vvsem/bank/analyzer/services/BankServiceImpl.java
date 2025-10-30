package ru.vvsem.bank.analyzer.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;
import ru.vvsem.bank.analyzer.mappers.BankMapper;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.repositories.BankRepository;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;

    private final BankMapper bankMapper;

    @Transactional(readOnly = true)
    @Override
    public List<BankDto> getBanks() {
        log.info("Getting banks");
        List<Bank> accounts = bankRepository.findAll();

        return accounts.stream()
                .map(bankMapper::toBankDto).toList();
    }

    @Override
    public Bank getBankById(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "Bank with id %d not found".formatted(id),
                                        "exception.entity.not.found.bank")
                );
    }
}
