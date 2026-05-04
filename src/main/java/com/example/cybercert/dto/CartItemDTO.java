package com.example.cybercert.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record CartItemDTO(
        @Schema(example = "1") Long id,
        @Schema(example = "10") Long certificationId,
        @Schema(example = "Security Essentials") String certificationName,
        @Schema(example = "89.99") BigDecimal price) {
}
