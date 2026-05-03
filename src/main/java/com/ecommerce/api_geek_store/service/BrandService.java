package com.ecommerce.api_geek_store.service;


import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import java.util.List;

public interface BrandService {
    List<BrandResponse> findAll();
    BrandResponse findById(Long id);
    BrandResponse create(BrandRequest request);
    void delete(Long id);
    BrandResponse update(Long id, BrandRequest request);
}