package com.ampt.controller;

import com.ampt.entity.AppUser;
import com.ampt.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    public static final String SESSION_USER_KEY = "currentUser";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (redirect != null && !redirect.isBlank()) {
            model.addAttribute("redirect", redirect);
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String redirect,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Optional<AppUser> userOpt = authService.authenticate(username, password);

        if (userOpt.isPresent()) {
            session.setAttribute(SESSION_USER_KEY, userOpt.get());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Welcome back, " + userOpt.get().getUsername() + "! (Role: " + userOpt.get().getRole() + ")");
            if (redirect != null && !redirect.isBlank() && !redirect.contains("login")) {
                return "redirect:" + redirect;
            }
            return "redirect:/prices";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password.");
            if (redirect != null && !redirect.isBlank()) {
                return "redirect:/login?redirect=" + redirect;
            }
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(SESSION_USER_KEY);
        session.invalidate();
        redirectAttributes.addFlashAttribute("infoMessage", "You have been logged out.");
        return "redirect:/login?logout=true";
    }
}
