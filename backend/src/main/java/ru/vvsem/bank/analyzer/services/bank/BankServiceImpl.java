package ru.vvsem.bank.analyzer.services.bank;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.mappers.BankMapper;
import ru.vvsem.bank.analyzer.models.Bank;
import ru.vvsem.bank.analyzer.models.enums.EntityName;
import ru.vvsem.bank.analyzer.providers.EntityAccessProvider;
import ru.vvsem.bank.analyzer.repositories.BankRepository;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;

    private final BankMapper bankMapper;

    private final EntityAccessProvider entityAccessProvider;

    @Override
    public List<BankDto> getBanks() {
        log.info("Getting banks");
        List<Bank> accounts = bankRepository.findAll();

        return accounts.stream()
                .map(bankMapper::toBankDto).toList();
    }

    @Override
    public Bank findById(Long bankId) {
        if (bankId == null) {
            throw new IllegalArgumentException(
                    "Bank id must not be null");
        }
        return bankRepository.findById(bankId)
                .orElseThrow(() ->
                        entityAccessProvider.entityNotFound(EntityName.BANK, bankId));
    }

}
