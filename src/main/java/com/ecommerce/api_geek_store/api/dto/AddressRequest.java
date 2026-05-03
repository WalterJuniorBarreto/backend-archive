package com.ecommerce.api_geek_store.api.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.*;

public record AddressRequest(
        @NotBlank(message = "El departamento es obligatorio")
        @Size(max = 100, message = "El departamento no puede tener más de 100 caracteres")
        String departamento,
        @NotBlank(message = "La provincia es obligatoria")
        @Size(max = 100, message = "La provincia no puede tener más de 100 caracteres")
        String provincia,
        @NotBlank(message = "El distrito es obligatorio")
        @Size(max = 100, message = "El distrito no puede tener más de 100 caracteres")
        String distrito,
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede tener más de 200 caracteres")
        String direccion,
        @Size(max = 200, message = "La referencia no puede tener más de 200 caracteres")
        String referencia,
        @NotBlank(message = "El código postal es obligatorio")
        @Size(min = 3, max = 10, message = "El código postal debe tener entre 3 y 10 caracteres")
        @Pattern(regexp = "\\d+")
        String codigoPostal
){}


