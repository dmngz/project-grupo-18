package com.example.cybercert.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

import com.example.cybercert.models.Certification;
import com.example.cybercert.models.Image;
import com.example.cybercert.models.User;
import com.example.cybercert.services.CertificationService;
import com.example.cybercert.services.CommentService;
import com.example.cybercert.services.ImageService;
import com.example.cybercert.services.UserService;
import com.example.security.Role;

import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.transaction.Transactional;

@Controller
public class AdminController {



    @Autowired
    private UserService userService;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    // ADMIN PAGE
    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
        
        if (principal != null) {
            model.addAttribute("logged", true);

            User user = userService.findByUsername(principal.getName()).orElse(null);

            if (user != null) {
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
            }
        }

        List<User> users = userService.findAll();
        List<Certification> certifications = certificationService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("certifications", certifications);

        model.addAttribute("pageCss", "admin");

        if (principal != null) {
            model.addAttribute("logged", true);
        }

        return "admin";
    }



     @PostMapping("/admin/delete")
    @Transactional
    public String deleteUser(@RequestParam String username) {

        if (!"admin".equals(username)) {

            userService.deleteByUsername(username);
        
        }

        return "redirect:/admin";
    }

    @GetMapping("/403")
    public String error403() {
        return "error403";
    }

    @GetMapping("/admin/add_certi")
    public String showAddCertificateForm(Model model, Principal principal) {
        model.addAttribute("pageCss", "auth");

        if (principal != null) {
            model.addAttribute("logged", true);

            User user = userService.findByUsername(principal.getName()).orElse(null);

            if (user != null) {
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
            }
        }
        return "add_certi";
    }

    @PostMapping("/admin/add_certi")
    @Transactional
    public String addCertificate(
            @RequestParam String name,
            @RequestParam String level,
            @RequestParam int duration,
            @RequestParam String format,
            @RequestParam String language,
            @RequestParam String description,
            @RequestParam String requirements,
            @RequestParam String contents,
            @RequestParam double price,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        Image image = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            image = imageService.createImage(imageFile.getInputStream());
        }

        List<String> reqList = List.of(requirements.split(","));
        List<String> contList = List.of(contents.split(","));

        Certification cert = new Certification(
                name,
                level,
                duration,
                format,
                language,
                description,
                reqList,
                contList,
                image);

        cert.setPrice(price);
        certificationService.save(cert);

        return "redirect:/admin";
    }

}
