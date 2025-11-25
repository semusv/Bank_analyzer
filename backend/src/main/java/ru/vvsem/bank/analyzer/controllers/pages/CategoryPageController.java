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
public class CategoryPageController {

    private final CustomUserDetailsService userService;

    @GetMapping("/categories")
    public String accountsPage(@AuthenticationPrincipal SecurityUser securityUser, Model model) {
        User currentUser = userService.getUserById(securityUser.getId());

        model.addAttribute("userName", currentUser.getName() + " " + currentUser.getSurname());
        model.addAttribute("pageTitle", "Категории");
        model.addAttribute("activePage", "categories");
        return "categories";
    }
}
