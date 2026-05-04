package com.example.cybercert.controllers.rest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.cybercert.dto.CartItemDTO;
import com.example.cybercert.dto.CreateCartItemDTO;
import com.example.cybercert.models.Certification;
import com.example.cybercert.models.ShoppingCartItem;
import com.example.cybercert.models.User;
import com.example.cybercert.services.CertificationService;
import com.example.cybercert.services.ShoppingCartService;
import com.example.cybercert.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cart-items")
public class ShoppingCartRestController {

    private final ShoppingCartService shoppingCartService;
    private final UserService userService;
    private final CertificationService certificationService;

    public ShoppingCartRestController(ShoppingCartService shoppingCartService, UserService userService,
            CertificationService certificationService) {
        this.shoppingCartService = shoppingCartService;
        this.userService = userService;
        this.certificationService = certificationService;
    }

    @GetMapping
    public ResponseEntity<Page<CartItemDTO>> getCartItems(Pageable pageable, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<CartItemDTO> dtoPage = shoppingCartService.findCartItems(user.getId(), pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable Long id, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return shoppingCartService.findCartItem(user.getId(), id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CartItemDTO> addToCart(@Valid @RequestBody CreateCartItemDTO request, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Certification certification = certificationService.findById(request.certificationId()).orElse(null);
        if (certification == null) {
            return ResponseEntity.notFound().build();
        }

        if (shoppingCartService.hasPurchasedCertification(user.getId(), certification.getId())
                || shoppingCartService.isCertificationInCart(user.getId(), certification.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        ShoppingCartItem cartItem = shoppingCartService.addToCart(user, certification);
        if (cartItem == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        CartItemDTO responseBody = toDTO(cartItem);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseBody.id())
                .toUri();

        return ResponseEntity.created(location).body(responseBody);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean removed = shoppingCartService.removeCartItem(user.getId(), id);
        if (!removed) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }

        return userService.findByUsername(principal.getName()).orElse(null);
    }

    private CartItemDTO toDTO(ShoppingCartItem cartItem) {
        String certificationName = cartItem.getCertification() != null ? cartItem.getCertification().getName() : null;
        Long certificationId = cartItem.getCertification() != null ? cartItem.getCertification().getId() : null;
        BigDecimal price = cartItem.getPrice() == null ? null
                : BigDecimal.valueOf(cartItem.getPrice()).setScale(2, RoundingMode.HALF_UP);

        return new CartItemDTO(cartItem.getId(), certificationId, certificationName, price);
    }
}
