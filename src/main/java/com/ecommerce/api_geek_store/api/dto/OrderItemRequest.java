package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        Long productId,

        @NotNull(message = "El ID de la variante es obligatorio (Color/Talla)")
        Long variantId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser al menos 1")
        Integer cantidad
) {}