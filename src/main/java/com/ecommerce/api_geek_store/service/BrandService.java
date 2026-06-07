package com.ecommerce.api_geek_store.service;


import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BrandService {
    Page<BrandResponse> findAll(String searchTerm, String statusFilter, Pageable pageable);
    BrandResponse findById(Long id);
    BrandResponse create(BrandRequest request);
    void delete(Long id);
    BrandResponse update(Long id, BrandRequest request);
    void activar(Long id);
}


