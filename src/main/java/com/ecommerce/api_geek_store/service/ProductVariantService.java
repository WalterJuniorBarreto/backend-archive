package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.request.ProductVariantRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductVariantResponse;

public interface ProductVariantService {
    ProductVariantResponse addVariantToProduct(Long productId, ProductVariantRequest request);
    ProductVariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request);
}
