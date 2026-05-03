package com.ecommerce.api_geek_store.api.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
        @NotBlank(message = "La calle es obligatoria") String calle,
        @NotBlank(message = "La ciudad es obligatoria") String ciudad,
        @NotBlank(message = "El estado/región es obligatorio") String estado,
        @NotBlank(message = "El código postal es obligatorio")
        @Size(min = 3, max = 10) String codigoPostal,
        @NotBlank(message = "El país es obligatorio") String pais
) {}