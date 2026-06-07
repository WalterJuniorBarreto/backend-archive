package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.api.mapper.BrandMapper;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class BrandServiceImpl implements BrandService {


    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;


    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> findAll(String searchTerm, String statusFilter, Pageable pageable) {
        log.debug("Consultando marcas con filtros - Busqueda: '{}', Estado: '{}'", searchTerm, statusFilter);

        return brandRepository.findWithFilters(searchTerm, statusFilter, pageable)
                .map(brandMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "brandCache", key = "#id")
    public BrandResponse findById(Long id) {
        log.debug("Cache Miss: Buscando marca en la Base de Datos con ID: {}", id);
        return brandRepository.findById(id)
                .map(brandMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Marca no encontrada con el identificador: %d", id)
                ));
    }

    @Override
    @Transactional
    @CacheEvict(value = "brandCache", allEntries = true)
    public BrandResponse create(BrandRequest request) {
        if(brandRepository.existsByNombreIgnoreCase(request.nombre())){
            log.warn("Intento de crear marca duplicada: {}", request.nombre());
            throw new DuplicateResourceException(
                    String.format("Ya existe una marca registrada con el nombre: %s", request.nombre())
            );
        }
        Brand brand = brandMapper.toEntity(request);
        Brand savedBrand = brandRepository.save(brand);

        log.info("Marca persistida exitosamente en DB: ID {} - {}", savedBrand.getId(), savedBrand.getNombre());

        return brandMapper.toResponse(savedBrand);
    }




    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "brandCache", allEntries = true),
            @CacheEvict(value = "brandCache", key = "#id")
    })
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Marca no encontrada con id: %d", id)
                ));

        if (!brand.getNombre().equalsIgnoreCase(request.nombre()) &&
                brandRepository.existsByNombreIgnoreCase(request.nombre())) {
            log.warn("COnflicto: Intento de renombrar marca ID {} a '{}' (Nombre ya en uso)", id, request.nombre());
            throw new DuplicateResourceException(
                    String.format("Ya existe la marca con ese nombre: %s", request.nombre())
            );
        }

        brand.setNombre(request.nombre());

        log.debug("Marca ID {} actualizada en memoria. Hibernate sincronizara con BD.", id);

        return brandMapper.toResponse(brand);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "brandCache", allEntries = true),
            @CacheEvict(value = "brandCache", key = "#id")
    })
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            log.warn("Fallo de eliminación: La marca ID {} no existe o ya fue desactivada.", id);
            throw new ResourceNotFoundException(
                    String.format("Marca no encontrada con id: %d", id)
            );        }

        brandRepository.deleteById(id);
        log.info("Operación exitosa: Marca ID {} desactivada lógicamente (Soft Delete).", id);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "brandCache", allEntries = true),
            @CacheEvict(value = "brandCache", key = "#id")
    })
    public void activar(Long id){
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Marca no encontrada con id: %d", id)
                ));

        if(brand.getActivo()){
            log.warn("La marca ID {} ya se encuentra activa.", id);
            throw new IllegalStateException("La marca ya esta activa en el sistema");
        }

        brand.setActivo(true);
        log.info("Operacion exitosa: Marca ID {} activada", id);
    }

}