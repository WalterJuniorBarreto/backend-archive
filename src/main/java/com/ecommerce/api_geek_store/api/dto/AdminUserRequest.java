package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 32, message = "La contraseña debe tener entre 8 y 32 caracteres")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
                message = "La contraseña debe contener al menos un dígito, una minúscula, una mayúscula y un carácter especial")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        @Pattern(regexp = "^(ADMIN|USER|SOPORTE)$", message = "El rol debe ser exactamente ADMIN, USER o SOPORTE")
        String rol
) {}