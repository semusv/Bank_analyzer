package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.dto.bank.BankDto;

import java.util.List;

public interface BanksService {

    List<BankDto> getBanks();
}
