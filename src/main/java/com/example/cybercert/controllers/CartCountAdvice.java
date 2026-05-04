package com.example.cybercert.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.cybercert.services.UserService;
import com.example.cybercert.services.ShoppingCartService;

@ControllerAdvice
public class CartCountAdvice {

    @Autowired
    private UserService userService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @ModelAttribute("cartCount")
    public int cartCount(Principal principal) {
        if (principal == null) {
            return 0;
        }

        return userService.findByUsername(principal.getName())
                .map(user -> shoppingCartService.getVisibleCartSize(user.getId()))
                .orElse(0);
    }
}