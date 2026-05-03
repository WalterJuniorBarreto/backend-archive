package com.ecommerce.api_geek_store.api.mapper;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.domain.model.Category;
import org.springframework.stereotype.Component;


@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequest request){
        if(request == null){
            return null;
        }
        Category category = new Category();
        category.setNombre(request.nombre().trim());
        category.setDescripcion(request.descripcion());
        return category;
    }


    public CategoryResponse toResponse(Category category){
        if(category == null){
            return null;
        }
        return new CategoryResponse(
                category.getId(),
                category.getNombre(),
                category.getDescripcion()
        );
    }

}
