package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.configs.BankThemeConfig;

@RequiredArgsConstructor
@Service
public class BankThemeServiceImpl implements BankThemeService {


    @Override
    public BankThemeConfig.BankTheme getBankTheme(String bankCode) {
        return null;
    }
}
