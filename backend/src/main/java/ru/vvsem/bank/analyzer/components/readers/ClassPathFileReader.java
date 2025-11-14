package ru.vvsem.bank.analyzer.components.readers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.vvsem.bank.analyzer.exceptions.BusinessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClassPathFileReader {

    public boolean fileExists(String filePath) {
        return new ClassPathResource(filePath).exists();
    }

    public String readFile(String filePath) {
        ClassPathResource resource = new ClassPathResource(filePath);
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining("\r\n"));

        } catch (IOException e) {
            log.error("Failed to read file: {}", filePath, e);
            throw new BusinessException(
                    "Can't read file: " + filePath,
                    "business.file.cant.read",
                    Map.of("filePath", filePath)
            );
        }
    }
}