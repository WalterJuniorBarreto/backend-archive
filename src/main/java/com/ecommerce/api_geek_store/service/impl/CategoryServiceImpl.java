package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.CategoryRequest;
import com.ecommerce.api_geek_store.api.dto.CategoryResponse;
import com.ecommerce.api_geek_store.api.mapper.CategoryMapper;
import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll(){
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + id));

        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse create(CategoryRequest categoryRequest) {


        Category category = categoryMapper.toEntity(categoryRequest);
        Category categoryGuardada = categoryRepository.save(category);

        log.info("Nueva categoría creada: ID {} - {}", categoryGuardada.getId(), categoryGuardada.getNombre());

        return categoryMapper.toResponse(categoryGuardada);
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest categoryRequest) {
        Category categoryExistente = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada con id: " + id));

        String nombreAntiguo = categoryExistente.getNombre();

        categoryExistente.setNombre(categoryRequest.nombre());
        categoryExistente.setDescripcion(categoryRequest.descripcion());

        Category categoryActualizada = categoryRepository.save(categoryExistente);

        log.info("Categoría actualizada ID {}: '{}' -> '{}'", id, nombreAntiguo, categoryActualizada.getNombre());

        return categoryMapper.toResponse(categoryActualizada);
    }

    @Override
    public void deleteById(Long id) {
        if(!categoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Categoria no encontrada con id: " + id);
        }

        try {
            categoryRepository.deleteById(id);
            log.info("Categoría eliminada ID: {}", id);

        } catch (DataIntegrityViolationException e) {

            log.warn("Intento fallido de borrar categoría ID {}: Tiene productos asociados.", id);
            throw new IllegalStateException("No se puede eliminar la categoría porque contiene productos asignados.");
        }
    }
}