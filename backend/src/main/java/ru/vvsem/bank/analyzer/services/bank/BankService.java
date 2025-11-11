package ru.vvsem.bank.analyzer.services.bank;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.models.Bank;

import java.util.List;

public interface BankService {

    @Transactional(readOnly = true)
    List<BankDto> getBanks();

    @Transactional(readOnly = true)
    Bank findById(Long bankId);

}
