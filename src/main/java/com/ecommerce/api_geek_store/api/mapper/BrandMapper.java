package com.ecommerce.api_geek_store.api.mapper;


import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.domain.model.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {

    @Mapping(target = "id", ignore = true)
    Brand toEntity(BrandRequest request);
    BrandResponse toResponse(Brand brand);
}
