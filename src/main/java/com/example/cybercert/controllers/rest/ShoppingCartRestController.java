package com.example.cybercert.controllers.rest;

import java.net.URI;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cybercert.dto.ShoppingCartDTO;
import com.example.cybercert.dto.ShoppingCartItemDTO;
import com.example.cybercert.dto.ShoppingCartPageDTO;
import com.example.cybercert.models.ShoppingCartItem;
import com.example.cybercert.models.Certification;
import com.example.cybercert.models.User;
import com.example.cybercert.services.CertificationService;
import com.example.cybercert.services.ShoppingCartService;
import com.example.cybercert.services.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping({"/api/v1/shopping-cart", "/api/v1/shopping-carts"})
@Tag(name = "Shopping cart", description = "Operaciones REST del carrito de compras")
public class ShoppingCartRestController {

    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final CertificationService certificationService;

    public ShoppingCartRestController(ShoppingCartService shoppingCartService,
            UserService userService,
            CertificationService certificationService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.certificationService = certificationService;
    }

    @GetMapping
    public ResponseEntity<ShoppingCartPageDTO> getCart(Pageable pageable, Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(buildCart(user.getId(), pageable));
    }

    @GetMapping("/items/{certificationId}")
    public ResponseEntity<ShoppingCartItemDTO> getCartItem(@PathVariable Long certificationId, Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var item = shoppingCartService.findCartItem(user.getId(), certificationId);
        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDto(item.get()));
    }

    @PostMapping("/items/{certificationId}")
    public ResponseEntity<ShoppingCartItemDTO> addToCart(@PathVariable Long certificationId, Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Certification certification = certificationService.findById(certificationId).orElse(null);
        if (certification == null) {
            return ResponseEntity.notFound().build();
        }

        if (shoppingCartService.hasPurchasedCertification(user.getId(), certificationId)
                || shoppingCartService.isCertificationInCart(user.getId(), certificationId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        shoppingCartService.addToCart(user, certification);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(toDto(certification));
    }

    @DeleteMapping("/items/{certificationId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long certificationId, Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (shoppingCartService.findCartItem(user.getId(), certificationId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        shoppingCartService.removeFromCart(user.getId(), certificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Void> checkout(Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        shoppingCartService.completeCheckout(user);
        return ResponseEntity.noContent().build();
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.findByUsername(principal.getName()).orElse(null);
    }

    private ShoppingCartPageDTO buildCart(Long userId, Pageable pageable) {
        shoppingCartService.removeOwnedCertificationsFromCart(userId);

        var page = shoppingCartService.getCartItemsPage(userId, pageable);
        List<ShoppingCartItemDTO> cartItems = new ArrayList<>();

        BigDecimal itemPrice = BigDecimal.valueOf(ShoppingCartService.CERTIFICATION_PRICE).setScale(2,
                RoundingMode.HALF_UP);

        page.getContent().forEach(item -> cartItems.add(toDto(item, itemPrice)));

        BigDecimal subtotal = BigDecimal.valueOf(ShoppingCartService.CERTIFICATION_PRICE)
                .multiply(BigDecimal.valueOf(cartItems.size()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        ShoppingCartDTO totals = new ShoppingCartDTO(cartItems, subtotal, discount, total, cartItems.size());
        return new ShoppingCartPageDTO(
                totals.cartItems(),
                totals.subtotal(),
                totals.discount(),
                totals.total(),
                totals.cartSize(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private ShoppingCartItemDTO toDto(ShoppingCartItem item) {
        return toDto(item, BigDecimal.valueOf(ShoppingCartService.CERTIFICATION_PRICE).setScale(2, RoundingMode.HALF_UP));
    }

    private ShoppingCartItemDTO toDto(ShoppingCartItem item, BigDecimal itemPrice) {
        Certification certification = item.getCertification();
        if (certification == null) {
            return new ShoppingCartItemDTO(item.getId(), null, null, itemPrice);
        }

        return new ShoppingCartItemDTO(
                certification.getId(),
                certification.getName(),
                certification.getDescription(),
                itemPrice);
    }

    private ShoppingCartItemDTO toDto(Certification certification) {
        return new ShoppingCartItemDTO(
                certification.getId(),
                certification.getName(),
                certification.getDescription(),
                BigDecimal.valueOf(ShoppingCartService.CERTIFICATION_PRICE).setScale(2, RoundingMode.HALF_UP));
    }
}