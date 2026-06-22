package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.response.ProductImageResponse;
import com.ecommerce.api_geek_store.domain.model.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {
    ProductImageResponse toResponse(ProductImage productImage);
}
