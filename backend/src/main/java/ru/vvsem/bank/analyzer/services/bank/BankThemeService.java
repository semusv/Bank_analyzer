package ru.vvsem.bank.analyzer.services.bank;


import ru.vvsem.bank.analyzer.dto.BankThemeDto;

public interface BankThemeService {

    BankThemeDto getBankTheme(String bankCode);

}
