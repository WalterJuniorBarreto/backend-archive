package com.ecommerce.api_geek_store.api.mapper;


import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.domain.model.Brand;
import org.springframework.stereotype.Component;


@Component
public class BrandMapper {

    public Brand toEntity(BrandRequest request) {
        if (request == null) return null;
        Brand brand = new Brand();
        brand.setNombre(request.nombre().trim());
        return brand;
    }

    public BrandResponse toResponse(Brand brand) {
        if (brand == null) return null;
        return new BrandResponse(
                brand.getId(),
                brand.getNombre()
        );
    }
}