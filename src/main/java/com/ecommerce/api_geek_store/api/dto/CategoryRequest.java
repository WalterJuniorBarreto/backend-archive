package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest (
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 3, max = 50)
        String nombre,

        @Size(max = 200)
        String descripcion
){}