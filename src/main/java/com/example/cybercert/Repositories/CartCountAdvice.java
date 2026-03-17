package com.example.cybercert;

import com.example.cybercert.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class CartCountAdvice {

    @Autowired
    private UserService userService;

    @ModelAttribute("cartCount")
    public int cartCount(Principal principal) {
        if (principal == null) return 0;
        return userService.findByUsername(principal.getName())
                .map(user -> user.getCartItems().size())
                .orElse(0);
    }
}
