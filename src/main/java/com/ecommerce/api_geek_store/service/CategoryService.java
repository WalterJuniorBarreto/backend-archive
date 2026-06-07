package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    Page<CategoryResponse> findAll(String searchTerm, CategoryStatus status, Pageable pageable);
    CategoryResponse findById(Long id);
    CategoryResponse create(CategoryRequest categoryRequest);
    CategoryResponse update(Long id, CategoryRequest categoryRequest);
    void deleteById(Long id);
    void activate(Long id);
}
