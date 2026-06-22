package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.request.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.response.BrandResponse;
import com.ecommerce.api_geek_store.api.mapper.BrandMapper;
import com.ecommerce.api_geek_store.domain.model.Brand;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceImplTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandServiceImpl brandService;

    private Brand brand;
    private BrandRequest brandRequest;
    private BrandResponse brandResponse;

    @BeforeEach
    void setUp(){
        brand = new Brand("Nike");
        brand.setId(1L);
        brand.setActivo(true);

        brandRequest = new BrandRequest("Nike");
        brandResponse = new BrandResponse(1L, "Nike", true);
    }


    @Test
    @DisplayName("Debe crear una marca exitosamente cuando el nombre no existe")
    void create_WhenNameIsUnique_ShouldSaveAndReturnBrand(){

        when(brandRepository.existsByNombreIgnoreCase(brandRequest.nombre())).thenReturn(false);
        when(brandMapper.toEntity(brandRequest)).thenReturn(brand);
        when(brandRepository.save(any(Brand.class))).thenReturn(brand);
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        BrandResponse result = brandService.create(brandRequest);

        assertNotNull(result);
        assertEquals(brandResponse.nombre(), result.nombre());
        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException al intentar crear marca duplicada")
    void create_WhenNameExists_ShouldThrowException() {
        when(brandRepository.existsByNombreIgnoreCase(brandRequest.nombre())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> brandService.create(brandRequest));
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    @DisplayName("Debe retornar la marca si el ID existe")
    void findById_WhenIdExists_ShouldReturnBrand() {
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandMapper.toResponse(brand)).thenReturn(brandResponse);

        BrandResponse result = brandService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el ID no existe")
    void findById_WhenIdDoesNotExist_ShouldThrowException() {
        when(brandRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> brandService.findById(99L));
    }

    @Test
    @DisplayName("Debe actualizar el nombre de la marca exitosamente")
    void update_WhenValidData_ShouldUpdateAndReturnBrand() {
        BrandRequest updateRequest = new BrandRequest("Nike Pro");
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.existsByNombreIgnoreCase("Nike Pro")).thenReturn(false);
        when(brandMapper.toResponse(brand)).thenReturn(new BrandResponse(1L, "Nike Pro", true));

        BrandResponse result = brandService.update(1L, updateRequest);

        assertEquals("Nike Pro", result.nombre());
        assertEquals("Nike Pro", brand.getNombre());
    }

    @Test
    @DisplayName("Debe lanzar conflicto al actualizar con un nombre ya existente")
    void update_WhenNewNameAlreadyExists_ShouldThrowConflict() {
        BrandRequest updateRequest = new BrandRequest("Adidas");
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.existsByNombreIgnoreCase("Adidas")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> brandService.update(1L, updateRequest));
    }

    @Test
    @DisplayName("Debe reactivar la marca si estaba inactiva")
    void activar_WhenBrandIsInactive_ShouldSetActivoToTrue() {
        brand.setActivo(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        brandService.activar(1L);

        assertTrue(brand.getActivo());
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si se intenta reactivar una marca ya activa")
    void activar_WhenBrandIsAlreadyActive_ShouldThrowException() {
        brand.setActivo(true);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        assertThrows(IllegalStateException.class, () -> brandService.activar(1L));
    }

    @Test
    @DisplayName("Debe ejecutar el borrado si la marca existe")
    void delete_WhenIdExists_ShouldCallDelete() {
        when(brandRepository.existsById(1L)).thenReturn(true);

        brandService.delete(1L);

        verify(brandRepository, times(1)).deleteById(1L);
    }
}
