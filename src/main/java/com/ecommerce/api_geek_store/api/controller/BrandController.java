package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.BrandRequest;
import com.ecommerce.api_geek_store.api.dto.BrandResponse;
import com.ecommerce.api_geek_store.service.BrandService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private static final Logger log = LoggerFactory.getLogger(BrandController.class);
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        return ResponseEntity.ok(brandService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        log.info("Admin creando nueva marca: {}", request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }


    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
        log.info("Solicitud para actualizar marca ID: {} con datos: {}", id, request);
        return ResponseEntity.ok(brandService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        log.info("Solicitud para eliminar marca ID: {}", id);
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}