package com.ecommerce.api_geek_store.api.dto;


import java.math.BigDecimal;
import java.util.List;



public record ProductResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer descuento,
        BigDecimal precioFinal,
        String imagenUrl,
        List<String> images,
        Long categoryId,
        String categoryName,
        Long brandId,
        String brandName,
        String genero,
        Boolean featured,
        List<VariantResponse> variantes,
        Integer totalStock
) {
    public record VariantResponse(
            Long id,
            String color,
            String colorHex,
            String talla,
            Integer stock
    ) {}
}