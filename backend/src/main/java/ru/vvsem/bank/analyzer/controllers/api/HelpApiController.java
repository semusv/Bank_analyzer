package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.vvsem.bank.analyzer.services.help.HelpService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/help")
public class HelpApiController {

    private final HelpService helpService;

    @GetMapping("/readme")
    @ResponseStatus(HttpStatus.OK)
    public String getReadme() {
        return helpService.getReadme("README.md");
    }

}
