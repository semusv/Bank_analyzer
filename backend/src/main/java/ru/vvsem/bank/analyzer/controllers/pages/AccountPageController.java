package ru.vvsem.bank.analyzer.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vvsem.bank.analyzer.models.User;

@Controller
@RequiredArgsConstructor
public class AccountPageController {

    @GetMapping("/accounts")
    public String accountsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("userName", user.getName() + " " + user.getSurname());
        model.addAttribute("pageTitle", "Счета и карты");
        return "accounts";
    }
}
