package com.example.cybercert.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.cybercert.Role;
import com.example.cybercert.Models.Certification;
import com.example.cybercert.Models.User;
import com.example.cybercert.Services.CertificationService;
import com.example.cybercert.Services.UserService;

import java.security.Principal;
import java.util.Set;

import jakarta.transaction.Transactional;

@Controller
public class ShoppingController {

    @Autowired
    private UserService userService;

    @Autowired
    private CertificationService certificationService;

    // CHECKOUT PAGE
    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {
        model.addAttribute("pageCss", "checkout");
        model.addAttribute("cartTotal", "0.00");

        if (principal != null) {
            model.addAttribute("logged", true);
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
                Set<Certification> cart = user.getCartItems();
                double total = cart.stream().mapToDouble(c -> c.getPrice() != null ? c.getPrice() : 0.0).sum();
                model.addAttribute("cartItems", cart);
                model.addAttribute("cartTotal", String.format("%.2f", total));
            }
        }

        return "checkout";
    }

    // CONFIRM CHECKOUT — moves cart items to purchased
    @PostMapping("/checkout/confirm")
    @Transactional
    public String confirmCheckout(Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        user.getPurchasedCertifications().addAll(user.getCartItems());
        user.getCartItems().clear();
        userService.save(user);

        return "redirect:/profile";
    }

    // SHOPPING CART PAGE
    @GetMapping("/shoppingcart")
    public String shoppingcart(Model model, Principal principal) {
        model.addAttribute("pageCss", "shoping-cart");
        model.addAttribute("cartTotal", "0.00");

        if (principal != null) {
            model.addAttribute("logged", true);
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                model.addAttribute("isAdmin", user.getRole() == Role.ADMIN);
                Set<Certification> cart = user.getCartItems();
                double total = cart.stream().mapToDouble(c -> c.getPrice() != null ? c.getPrice() : 0.0).sum();
                model.addAttribute("cartItems", cart);
                model.addAttribute("cartTotal", String.format("%.2f", total));
                model.addAttribute("hasItems", !cart.isEmpty());
            }
        }

        return "shopping-cart";
    }

    // ADD TO CART
    @PostMapping("/cart/add/{id}")
    @Transactional
    public String addToCart(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Certification cert = certificationService.findById(id).orElse(null);
        if (cert == null) return "redirect:/";

        boolean alreadyPurchased = user.getPurchasedCertifications().stream()
                .anyMatch(c -> c.getId().equals(id));
        if (alreadyPurchased) return "redirect:/certification/" + id + "?already_purchased=true";

        boolean alreadyInCart = user.getCartItems().stream()
                .anyMatch(c -> c.getId().equals(id));
        if (!alreadyInCart) {
            user.getCartItems().add(cert);
            userService.save(user);
        }

        return "redirect:/shoppingcart";
    }

    // REMOVE FROM CART
    @PostMapping("/cart/remove/{id}")
    @Transactional
    public String removeFromCart(@PathVariable Long id, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        user.getCartItems().removeIf(c -> c.getId().equals(id));
        userService.save(user);

        return "redirect:/shoppingcart";
    }
}
