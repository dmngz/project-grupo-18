package com.example.cybercert.dto;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingCartPageDTO(
        List<ShoppingCartItemDTO> cartItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        int cartSize,
        int page,
        int size,
        long totalItems,
        int totalPages) {
}