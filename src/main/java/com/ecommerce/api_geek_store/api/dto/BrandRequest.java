package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        String nombre
) {}