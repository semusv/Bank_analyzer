package ru.vvsem.bank.analyzer.services;

import ru.vvsem.bank.analyzer.configs.BankThemeConfig.BankTheme;

public interface BankThemeService {

    BankTheme getBankTheme(String bankCode);

}
