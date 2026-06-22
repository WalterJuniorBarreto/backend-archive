package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.api.dto.request.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductResponse> findAllWithFilters(ProductSearchCriteria criteria, Pageable pageable);
    ProductResponse create(ProductRequest request);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void archiveProduct(Long id);
    void publishProduct(Long id);
    void deactivateProduct(Long id);

}
