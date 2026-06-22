package com.ecommerce.api_geek_store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload para registrar una nueva talla/color de zapatilla")
public record ProductVariantRequest(
        @Schema(description = "Color de la zapatilla", example = "Rojo/Blanco")
        @NotBlank(message = "El color es obligatorio")
        @Size(max = 30, message = "El color no puede exceder los 30 caracteres")
        String color,

        @Schema(description = "Talla específica", example = "US 8.5")
        @NotBlank(message = "La talla es obligatoria")
        @Size(max = 10, message = "La talla no puede exceder los 10 caracteres")
        String talla,

        @Schema(description = "Cantidad física disponible en almacén", example = "50")
        @NotNull(message = "El stock inicial es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @Schema(description = "Costo extra si es una edición especial o talla grande", example = "0.00")
        @Min(value = 0, message = "EL precio adicional no puede ser negativo")
        BigDecimal precioAdicional
) { }
