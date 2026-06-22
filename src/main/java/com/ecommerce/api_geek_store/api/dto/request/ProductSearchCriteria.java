package com.ecommerce.api_geek_store.api.dto.request;

import com.ecommerce.api_geek_store.domain.model.enums.Genero;
import com.ecommerce.api_geek_store.domain.model.enums.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record ProductSearchCriteria(

        @Schema(description = "Search by product name or keywords", example = "sambas adidas")
        String searchTerm,

        @Schema(description = "Filter by active/inactive status", example = "ACTIVO")
        ProductStatus status,

        @Schema(description = "Minium price filter", example = "100.00")
        @DecimalMin(value = "0.0", message = "El precio minimo no puede ser negativo")
        BigDecimal minPrice,

        @Schema(description = "Maximun price filter", example = "1500.00")
        @DecimalMin(value = "0.0", message = "El precio no puede ser nativo")
        BigDecimal maxPrice,

        @Schema(description = "List of category Ids to filter by")
        List<@Positive Long> categoryIds,

        @Schema(description = "List of brand ifs to filter by")
        List<@Positive Long> brandIds,

        @Schema(description = "Gender category: MALE, FEMALE, UNISEX", example = "UNISEX")
        Genero gender,

        @Schema(description = "Show only products currently in strock", example = "true")
        Boolean inStockOnly
) { }
