package com.example.cybercert.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingCartDTO(
        List<ShoppingCartItemDTO> cartItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        int cartSize) {
}