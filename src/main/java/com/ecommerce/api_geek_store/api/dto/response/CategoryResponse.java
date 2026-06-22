package com.ecommerce.api_geek_store.api.dto.response;

import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de la categoria")
public record CategoryResponse(
        @Schema(description = "ID unico autogenereado", example = "1")
        Long id,

        @Schema(description = "Nombre de la categoria", example = "Running")
        String nombre,

        @Schema(description = "EStado actual para borrado logico", example = "ACTIVO")
        CategoryStatus status
) { }

