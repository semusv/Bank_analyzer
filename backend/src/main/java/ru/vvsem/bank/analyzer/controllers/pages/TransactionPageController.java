package ru.vvsem.bank.analyzer.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

@Controller
@RequiredArgsConstructor
public class TransactionPageController {
    private final CustomUserDetailsService userService;

    @GetMapping("/transactions")
    public String accountsPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User currentUser = userService.getUserById(securityUser.getId());

        model.addAttribute("userName", currentUser.getName() + " " + currentUser.getSurname());
        model.addAttribute("pageTitle", "Транзакции");
        model.addAttribute("activePage", "transactions");
        return "transactions";
    }
}
