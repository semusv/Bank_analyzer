package ru.vvsem.bank.analyzer.controllers.pages;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String login(@RequestParam(value = "error", required = false) String error,
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

    @PostMapping("/register")
    public String register(User user, Model model) {
        try {
            userService.registerUser(user);
            model.addAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти.");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("userName", user.getName() + " " + user.getSurname());
        model.addAttribute("pageTitle", "Дашборд");
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
