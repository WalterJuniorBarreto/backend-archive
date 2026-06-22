package com.ecommerce.api_geek_store.domain.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Define el ciclo de vida exacto de la zapatilla en el e-commerce")
public enum ProductStatus {
    @Schema(description = "El producto se está creando. No es visible para los clientes.")
    DRAFT("Borrador"),

    @Schema(description = "El producto está publicado y listo para la venta.")
    ACTIVE("Activo"),

    @Schema(description = "El producto fue ocultado manualmente por el administrador.")
    INACTIVE("Inactivo"),


    @Schema(description = "El producto sigue visible por SEO, pero no permite compras.")
    OUT_OF_STOCK("Agotado"),

    @Schema(description = "El modelo fue descontinuado. No se borra de la BD por integridad referencial.")
    ARCHIVED("Archivado");

    private final String label;
}
