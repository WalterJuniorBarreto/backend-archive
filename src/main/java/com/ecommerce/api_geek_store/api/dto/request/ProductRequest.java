package com.ecommerce.api_geek_store.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Payload para la creacion de una nueva zapatilla en el catalogo")
public record ProductRequest(

        @Schema(description = "NOmbre oficial de la zapatilla", example = "Adidas Samba 06")
        @NotBlank(message = "El nombre no puede estar vacio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Schema(description = "Descripcion SEO detallada", example = "Zapatilla clasica con exterios de piel y detalles en ante...")
        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        @Schema(description = "Precio base de venta", example = "120.00")
        @NotNull(message = "El precio base es obligatoria")
        @Positive(message = "EL precio debe ser mayor a 0")
        BigDecimal precioBase,

        @Schema(description = "ID de la categoria", example = "1")
        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "ID de categoria invalido")
        Long categoryId,

        @Schema(description = "ID de la marca", example = "2")
        @NotNull(message = "La marca es obligaotira")
        @Positive(message = "ID de marca invalido")
        Long brandId,

        @Schema(description = "Genero: MALE, FEMALE, UNISEX", example = "UNISEX")
        @NotBlank(message = "El genero es obligatorio")
        String genero

) { }
