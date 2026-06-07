package com.ecommerce.api_geek_store.api.dto;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        String rol,
        String dni,
        String telefono,
        String genero,
        LocalDate fechaNacimiento,
        String authProvider,
        boolean isEnabled,
        LocalDateTime createdAt
) { }