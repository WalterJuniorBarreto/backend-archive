package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.request.ProductVariantRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductVariantResponse;
import com.ecommerce.api_geek_store.domain.model.ProductVariant;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariantMapper {

    ProductVariant toEntity(ProductVariantRequest request);
    ProductVariantResponse toResponse(ProductVariant variant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(ProductVariantRequest request, @MappingTarget ProductVariant variant);
}
