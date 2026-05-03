package com.ecommerce.api_geek_store.api.dto;

import com.ecommerce.api_geek_store.domain.model.Genero;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        String nombre,

        @Size(max = 2000)
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal precio,

        @Min(0) @Max(100)
        Integer descuento,

        @NotEmpty(message = "Debe haber al menos una imagen")
        List<String> images,

        @NotNull(message = "Categoría obligatoria")
        Long categoryId,

        Long brandId,
        Genero genero,
        Boolean featured,

        @NotEmpty(message = "Debe tener al menos una variante")
        @Valid
        List<VariantRequest> variantes
) {
    public record VariantRequest(
            @NotBlank String color,
            @NotBlank String colorHex,
            @NotBlank String talla,
            @NotNull @Min(0) Integer stock
    ) {}
}