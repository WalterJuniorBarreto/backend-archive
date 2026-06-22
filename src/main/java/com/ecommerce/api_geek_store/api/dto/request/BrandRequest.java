package com.ecommerce.api_geek_store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Objeto de transferencia de datos (Payload) para registrar o modificar una Marca.")
public record BrandRequest(
        @NotBlank(message = "El nombre de la marca es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre de tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9\\s\\-]+$", message = "El nombre contiene caracteres no permitidos")
        @Schema(
                description = "Nombre comercial de la marca. Se limpiarán los espacios en blanco al inicio y al final automáticamente.",
                example = "Nike",
                minLength = 2,
                maxLength = 50
        )
        String nombre
) {
        public BrandRequest{
                if(nombre != null){
                        nombre = nombre.trim();
                }
        }
}


