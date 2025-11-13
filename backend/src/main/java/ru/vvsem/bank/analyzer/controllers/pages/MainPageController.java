package ru.vvsem.bank.analyzer.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.vvsem.bank.analyzer.models.SecurityUser;
import ru.vvsem.bank.analyzer.models.User;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

@Controller
@RequiredArgsConstructor
public class MainPageController {

    private final CustomUserDetailsService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        if (logout != null) {
            model.addAttribute("message", "Вы успешно вышли из системы");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal SecurityUser securityUser, Model model) {

        User currentUser = userService.getUserById(securityUser.getId());

        model.addAttribute("userName", currentUser.getName() + " " + currentUser.getSurname());
        model.addAttribute("pageTitle", "Дашборд");
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
