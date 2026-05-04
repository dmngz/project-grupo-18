package com.example.cybercert.dto;

import java.math.BigDecimal;

public record ShoppingCartItemDTO(
        Long certificationId,
        String name,
        String description,
        BigDecimal price) {
}