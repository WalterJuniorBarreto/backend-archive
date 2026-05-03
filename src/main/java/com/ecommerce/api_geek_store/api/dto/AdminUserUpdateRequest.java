package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AdminUserUpdateRequest(
        String nombre,
        String apellido,
        @Email(message = "Formato de email inválido")
        String email,
        @Pattern(regexp = "ADMIN|USER|SOPORTE", message = "El rol debe ser ADMIN, USER o SOPORTE")
        String rol
) { }