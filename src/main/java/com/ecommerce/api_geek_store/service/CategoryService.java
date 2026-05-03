package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.domain.model.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> findAll();
    CategoryResponse findById(Long id);
    CategoryResponse create(CategoryRequest categoryRequest);
    CategoryResponse update(Long id, CategoryRequest categoryRequest);
    void deleteById(Long id);
}
