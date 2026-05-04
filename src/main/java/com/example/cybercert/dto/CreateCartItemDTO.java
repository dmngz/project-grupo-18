package com.example.cybercert.dto;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateCartItemDTO(
        @NotNull @Schema(example = "10") Long certificationId) {
}
