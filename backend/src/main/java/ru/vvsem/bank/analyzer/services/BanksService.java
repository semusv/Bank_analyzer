package ru.vvsem.bank.analyzer.services;

import org.springframework.transaction.annotation.Transactional;
import ru.vvsem.bank.analyzer.dto.BankDto;

import java.util.List;

public interface BanksService {

    List<BankDto> getBanks();
}
