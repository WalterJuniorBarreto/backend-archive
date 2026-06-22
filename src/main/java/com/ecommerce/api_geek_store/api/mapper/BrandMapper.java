package com.ecommerce.api_geek_store.api.mapper;


import com.ecommerce.api_geek_store.api.dto.request.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.response.BrandResponse;
import com.ecommerce.api_geek_store.domain.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    Brand toEntity(BrandRequest request);
    BrandResponse toResponse(Brand brand);
}
