package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.ProductResponse;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> findAll(Pageable pageable, String keyword, Long categoryId, Long brandId, String gender);
    ProductResponse findById(Long id);
    List<ProductResponse> findByCategoryId(Long categoryId);
    ProductResponse create(ProductRequest productRequest);
    ProductResponse update(Long id, ProductRequest productRequest);
    void deleteById(Long id);
    ProductResponse getFeatured();
}
