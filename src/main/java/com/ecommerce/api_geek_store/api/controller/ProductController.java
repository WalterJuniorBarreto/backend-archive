package com.ecommerce.api_geek_store.api.controller;

import com.ecommerce.api_geek_store.api.dto.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.ProductResponse;
import com.ecommerce.api_geek_store.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService){
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 12, sort = "nombre") Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String gender
    ) {
        log.debug("Listando productos - Filtros: Keyword={}, Category={}, Brand={}, Gender={}",
                keyword, categoryId, brandId, gender);

        Page<ProductResponse> productsPage = productService.findAll(pageable, keyword, categoryId, brandId, gender);
        return ResponseEntity.ok(productsPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest){
        log.info("Admin creando nuevo producto: {}", productRequest.nombre());
        ProductResponse response = productService.create(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest productRequest){

        log.info("Admin actualizando producto ID: {}", id);
        return ResponseEntity.ok(productService.update(id, productRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        log.warn("Admin eliminando producto ID: {}", id);
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<ProductResponse> getFeaturedProduct() {
        ProductResponse featuredProduct = productService.getFeatured();
        return featuredProduct != null ? ResponseEntity.ok(featuredProduct) : ResponseEntity.noContent().build();
    }
}

