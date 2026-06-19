package com.example.demo.controllers;

import com.example.demo.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalController {

    @ModelAttribute
    public void addAttributes(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        User newUser = (User) session.getAttribute("newUser");

        boolean isLoggedIn = currentUser != null;
        boolean isRegistered = newUser != null;

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("isRegistered", isRegistered);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("newUser", newUser);
    }
}