package ru.vvsem.bank.analyzer.services.bank;


import ru.vvsem.bank.analyzer.dto.bank.BankDto;
import ru.vvsem.bank.analyzer.models.Bank;

import java.util.List;

public interface BankService {

    List<BankDto> getBankDtos();

    Bank findById(Long bankId);

    BankDto findBankDtoById(Long bankId);

}
