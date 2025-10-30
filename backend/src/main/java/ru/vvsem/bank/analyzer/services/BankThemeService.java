package ru.vvsem.bank.analyzer.services;


import ru.vvsem.bank.analyzer.dto.BankThemeDto;

public interface BankThemeService {

    BankThemeDto getBankTheme(String bankCode);

}
