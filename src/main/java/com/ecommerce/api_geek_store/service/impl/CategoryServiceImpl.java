package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.api.mapper.CategoryMapper;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAll(String searchTerm, CategoryStatus status, Pageable pageable){

        log.info("COnsultando categorias en BD = Busqueda: {} Estado: {}", searchTerm, status);

        Page<Category> categories = categoryRepository.findCategoriesWithFilters(searchTerm, status, pageable);

        return categories.map(categoryMapper::toResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        log.info("COnsultando categoria en BD con ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Intento de busqueda fallido: Categoria no encontrada con id {}", id);
                    return new ResourceNotFoundException(
                            String.format("Categoria no encontrada con id: %d", id)
                    );
                });

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest categoryRequest) {
        log.info("Procesando creacion de cateogria: {}", categoryRequest.nombre());

        if(categoryRepository.existsByNombreIgnoreCase(categoryRequest.nombre())){
            log.warn("INtento de creacion fallido: La categoria {} ya existe", categoryRequest.nombre());
            throw new DuplicateResourceException(
                    String.format("Ya existe una categoria con el nombre %s", categoryRequest.nombre())
            );
        }

        Category category = categoryMapper.toEntity(categoryRequest);
        Category categoryGuardada = categoryRepository.save(category);

        log.info("Nueva categoría creada: ID {} - {}", categoryGuardada.getId(), categoryGuardada.getNombre());
        return categoryMapper.toResponse(categoryGuardada);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest categoryRequest) {

        log.info("Procesando actualizacion para categoria ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Categoria no encontrada %d",id)
                ));

        if(!category.getNombre().equalsIgnoreCase(categoryRequest.nombre()) &&
            categoryRepository.existsByNombreIgnoreCase(categoryRequest.nombre())){

            log.warn("Conflicto: Intento de renombrar a {}, pero ya existe", categoryRequest.nombre());
            throw new DuplicateResourceException(
                    String.format("Ya existe otra categoria con el nombre: %s", categoryRequest.nombre())
            );
        }

        category.setNombre(categoryRequest.nombre().trim());

        log.debug("Categoria ID {} actualizada en memoria. Hibernate sincronizara con BD.", id);

        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Procesando desactivacion SOFT DELETE para categoria por ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fallo al desactivar: Categoria no encontrada con id {}", id);
                    return new ResourceNotFoundException(
                            String.format("Categoria no encontrada con id: %d", id)
                    );
                });

        if(category.getStatus() == CategoryStatus.INACTIVO){
            log.info("La categoria ID {} ya se encontraba inactiva. No se requiere actualizacion", id);
            return;
        }

        category.setStatus(CategoryStatus.INACTIVO);

        log.info("Categoria ID: {} desactivada exitosamente", id);
    }

    @Override
    @Transactional
    public void activate(Long id){
        log.info("Procesando activacion para categoria ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fallo al activar. Categoria no encontrada con id {}", id);
                    return new ResourceNotFoundException(
                            String.format("Categoria no encontrada con id %d", id)
                    );
                });

        if(category.getStatus() == CategoryStatus.ACTIVO){
            log.info("La categoria ID {} ya se encontrada activa. No se requiere actualizacion", id);
            return;
        }

        category.setStatus(CategoryStatus.ACTIVO);

        log.info("Categoria ID: {} activada exitosamente", id);
    }
}