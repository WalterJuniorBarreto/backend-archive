package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.api.mapper.BrandMapper;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper) {
        this.brandRepository = brandRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream()
                .map(brandMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BrandResponse create(BrandRequest request) {
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);

        log.info("Nueva marca creada: ID {} - {}", savedBrand.getId(), savedBrand.getNombre());

        return brandMapper.toResponse(savedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        return brandRepository.findById(id)
                .map(brandMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));
    }

    @Override
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            log.warn("Intento de eliminar marca inexistente ID: {}", id);
            throw new ResourceNotFoundException("Marca no encontrada con id: " + id);
        }

        try {
            brandRepository.deleteById(id);
            log.info("Marca eliminada ID: {}", id);

        } catch (DataIntegrityViolationException e) {
            log.warn("Bloqueo de eliminación: La marca ID {} tiene productos asociados.", id);

            throw new IllegalStateException("No se puede eliminar la marca porque tiene productos asociados.");
        }
    }
    @Override
    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        // 1. Buscar si existe
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada con id: " + id));

        // 2. Validar que el nuevo nombre no esté duplicado (si cambió)
        if (!brand.getNombre().equalsIgnoreCase(request.nombre()) &&
                brandRepository.existsByNombre(request.nombre())) {
            throw new IllegalArgumentException("Ya existe una marca con el nombre: " + request.nombre());
        }

        // 3. Actualizar
        brand.setNombre(request.nombre());

        // 4. Guardar y retornar
        return brandMapper.toResponse(brandRepository.save(brand));
    }
}