package ru.vvsem.bank.analyzer.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.configs.BankThemeConfig;
import ru.vvsem.bank.analyzer.dto.BankThemeDto;
import ru.vvsem.bank.analyzer.mappers.BankThemeMapper;
import ru.vvsem.bank.analyzer.models.BankTheme;

@RequiredArgsConstructor
@Service
public class BankThemeServiceImpl implements BankThemeService {

    private final BankThemeConfig bankThemeConfig;

    private final BankThemeMapper bankThemeMapper;

    @Override
    public BankThemeDto getBankTheme(String bankCode) {
        BankTheme theme = bankThemeConfig.getThemeForBank(bankCode);
        BankThemeDto dto = bankThemeMapper.toBankThemeDto(theme);
        return dto;
    }
}
