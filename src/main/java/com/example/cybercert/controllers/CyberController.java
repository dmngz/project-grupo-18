package com.example.cybercert.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.cybercert.models.Certification;
import com.example.cybercert.models.User;
import com.example.cybercert.services.CertificationService;
import com.example.cybercert.services.CommentService;
import com.example.cybercert.services.UserService;
import com.example.security.Role;

@Controller
public class CyberController {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // HOME
    @GetMapping("/")
    public String index(Model model, Principal principal) {
        model.addAttribute("pageCss", "index");

        if (principal != null) {
            model.addAttribute("logged", true);
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
            }
        }

        List<Certification> certifications = certificationService.findAll();
        certifications.removeIf(certification -> certification.getImage() == null || certification.getImage().getId() == null);
        model.addAttribute("certifications", certifications);

        return "index";
    }

}
