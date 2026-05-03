package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank(message = "El email no puede estar vacio")
        String email,
        @NotBlank(message = "El codigo no puede estar vacio")
        String code,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", message = "La contraseña debne contener al menos una letra mayuscula, una minuscula y un numero")
        String newPassword
) {}

