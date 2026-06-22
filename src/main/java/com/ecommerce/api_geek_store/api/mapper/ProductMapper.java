package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.domain.model.Product;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    //product.getCategory().setId()
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "status", constant = "DRAFT")
    Product toEntity(ProductRequest request);

    @Mapping(target = "totalStock", expression = "java(product.getTotalStock())")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "brand.id", source = "brandId")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "variants", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
