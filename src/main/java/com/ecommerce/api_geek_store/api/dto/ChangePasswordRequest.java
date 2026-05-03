package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña antigua es obligatoria")
        String oldPassword,

        @NotBlank(message = "La contraseña nueva es obligatoria")
        @Size(min = 8, max = 32, message = "La nueva contraseña debe tener entre 8 y 32 caracteres")
        String newPassword
) {}