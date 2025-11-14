package ru.vvsem.bank.analyzer.services.help;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vvsem.bank.analyzer.components.readers.ClassPathFileReader;
import ru.vvsem.bank.analyzer.exceptions.EntityNotFoundException;

import java.util.Map;


@RequiredArgsConstructor
@Service
@Slf4j
public class HelpServiceImpl implements HelpService {

    private final ClassPathFileReader fileReader;

    @Override
    public String getReadme(@NotNull String fileName) {

        if (!fileReader.fileExists(fileName)) {
            throw new EntityNotFoundException(
                    "File not found: " + fileName,
                    "exception.entity.not.found.readme",
                    Map.of("fileName", fileName)
            );
        }

        return fileReader.readFile(fileName);
    }
}
