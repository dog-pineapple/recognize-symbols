package com.example.demo.controllers;

import com.example.demo.model.NoteList;
import com.example.demo.model.User;
import com.example.demo.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/login")
    public String processLogin(@RequestParam String login,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        System.out.println("Login: " + login);
        System.out.println("Password: " + password);
        Optional<User> userOpt = userRepository.findByLogin(login);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Нет такого пользователя");
            return "interactive";
        }

        User user = userOpt.get();
        System.out.println("User found: " + user.getLogin());
        System.out.println("Stored password: " + user.getPassword());
        if (!password.equals(user.getPassword())) {
            model.addAttribute("error", "Неверный логин или пароль");
            return "interactive";
        }
        session.setAttribute("currentUser", user);
        session.removeAttribute("newUser");
        return "redirect:/";
    }


    @PostMapping("/register")
    public String registration(@RequestParam String login,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {

        if (login == null || login.trim().isEmpty()) {
            model.addAttribute("error", "введи логин");
            return "interactive";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "введи пароль");
            return "interactive";
        }

        Optional<User> existingUser = userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            model.addAttribute("error", "Логин занят");
            return "interactive";
        }

        User newUser = new User(login, password);
        userRepository.save(newUser);

        session.setAttribute("newUser", newUser);

        return "redirect:/";
    }


    @GetMapping("/account")
    public String getAccount(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }
        User user = userRepository.findById(currentUser.getId()).get();
        List<NoteList> noteList = user.getNoteLists();

        model.addAttribute("user", currentUser);
        model.addAttribute("noteList", noteList);
        model.addAttribute("currentIndex", 0);
        model.addAttribute("length", noteList.size());

        return "usercabinet";
    }
    @GetMapping("/admin/db")
    @ResponseBody
    public String showDatabaseContent() {
        List<User> allUsers = userRepository.findAll();

        StringBuilder result = new StringBuilder();
        result.append("<h1>Содержимое БД</h1>");
        result.append("<table border='1'>");
        result.append("<tr><th>ID</th><th>Login</th><th>Password</th></tr>");

        for (User user : allUsers) {
            result.append("<tr>")
                    .append("<td>").append(user.getId()).append("</td>")
                    .append("<td>").append(user.getLogin()).append("</td>")
                    .append("<td>").append(user.getPassword()).append("</td>")
                    .append("</tr>");
        }

        result.append("</table>");
        return result.toString();
    }

}
