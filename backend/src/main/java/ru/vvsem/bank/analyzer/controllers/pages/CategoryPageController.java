package ru.vvsem.bank.analyzer.controllers.pages;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vvsem.bank.analyzer.models.User;

@Controller
public class CategoryPageController {
    @GetMapping("/categories")
    public String accountsPage(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("userName", user.getName() + " " + user.getSurname());
        model.addAttribute("pageTitle", "Категории");
        model.addAttribute("activePage", "categories");
        return "categories";
    }
}
