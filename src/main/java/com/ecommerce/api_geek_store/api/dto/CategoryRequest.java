package com.ecommerce.api_geek_store.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para la creacion o atualizacion de una categoria")
public record CategoryRequest (

        @Schema(description = "NOmbre unico de la categoria", example = "Running", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 3, max = 50, message = "El nombre debe de tener entre 3 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9ñÑáéíóúÁÉÍÓÚ ]+$", message = "El nombre contiene caracteres no permitidos")
        String nombre
) {
        public CategoryRequest {
                if (nombre != null) {
                        nombre = nombre.trim();
                }
        }
}