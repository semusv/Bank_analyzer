package ru.vvsem.bank.analyzer.controllers.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DashboardsController {

    @GetMapping("/api/dashboard/stats")
    public String getStats() {
        return null;
    }

}
