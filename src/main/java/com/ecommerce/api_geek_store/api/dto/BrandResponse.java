package com.ecommerce.api_geek_store.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de respuesta que representa los datos de una Marca en el catálogo.")
public record BrandResponse(

        @Schema(description = "Identificador único y autogenerado de la marca", example = "1")
        Long id,

        @Schema(description = "Nombre comercial registrado", example = "Adidas")
        String nombre,

        @Schema(description = "Estado de disponibilidad en el sistema. True si está visible, False si fue dada de baja (Soft Delete).", example = "true")
        @JsonProperty("activo")
        Boolean activo
) {}


