package ru.vvsem.bank.analyzer.services.help;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.exceptions.BusinessException;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Service
@Slf4j
public class HelpServiceImpl implements HelpService {

    @Override
    public String getReadme() {
        ClassPathResource resource = new ClassPathResource("README.md");
        if (!resource.exists()) {
            throw new EntityNotFoundException(
                    "File not found",
                    "exception.entity.not.found.readme"
            );
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
            ).lines().reduce("", (acc, line) -> acc + line + "\n");
        } catch (IOException e) {
            throw new BusinessException(
                    "Can't read README.md",
                    "business.help.readme.cant.read"
            );
        }
    }
}
