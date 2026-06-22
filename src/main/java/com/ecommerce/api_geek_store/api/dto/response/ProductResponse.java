package com.ecommerce.api_geek_store.api.dto.response;


import java.math.BigDecimal;
import java.util.List;


public record ProductResponse(
        Long id,
        String slug,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        Integer descuento,
        String genero,
        String status,
        Boolean featured,
        Integer totalStock,
        CategoryResponse category,
        BrandResponse brand,
        List<ProductVariantResponse> variants,
        List<ProductImageResponse> images
) {}