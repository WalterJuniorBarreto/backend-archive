package com.ecommerce.api_geek_store.api.dto;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
        String nombre,
        String apellido,
        String dni,
        String telefono,
        String genero,
        LocalDate fechaNacimiento
) {}