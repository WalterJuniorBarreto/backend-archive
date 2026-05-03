package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.ProductResponse;
import com.ecommerce.api_geek_store.api.mapper.ProductMapper;
import com.ecommerce.api_geek_store.domain.model.*;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              BrandRepository brandRepository,
                              ProductMapper productMapper){
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(Pageable pageable, String keyword, Long categoryId, Long brandId, String gender) {
        Genero generoEnum = null;

        log.debug("Buscando productos: Keyword={}, Cat={}, Brand={}, Gender={}", keyword, categoryId, brandId, gender);

        if (gender != null && !gender.isBlank()) {
            try {
                if (gender.equalsIgnoreCase("Men")) generoEnum = Genero.HOMBRE;
                else if (gender.equalsIgnoreCase("Women")) generoEnum = Genero.MUJER;
                else generoEnum = Genero.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Género inválido recibido: '{}'. Se ignorará el filtro.", gender);
                generoEnum = null;
            }
        }

        Page<Product> productPage = productRepository.buscarConFiltros(
                categoryId,
                brandId,
                generoEnum,
                keyword,
                pageable
        );

        return productPage.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {

        Product product = productRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByCategoryId(Long categoryId) {
        if(!categoryRepository.existsById(categoryId)){
            throw new ResourceNotFoundException("Categoria no encontrada con id: " + categoryId);
        }

        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getFeatured() {
        return productRepository.findFirstByFeaturedTrue()
                .map(productMapper::toResponse)
                .orElse(null);
    }

    @Override
    public ProductResponse create(ProductRequest productRequest) {
        log.info("Creando nuevo producto: {}", productRequest.nombre());

        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        if (Boolean.TRUE.equals(productRequest.featured())) {
            log.info("Nuevo producto destacado. Reseteando anteriores.");
            productRepository.resetFeatured();
        }

        Product product = productMapper.toEntity(productRequest);
        product.setCategory(category);

        if (productRequest.brandId() != null && productRequest.brandId() > 0) {
            Brand brand = brandRepository.findById(productRequest.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
            product.setBrand(brand);
        }

        Product productGuardado = productRepository.save(product);
        log.info("Producto creado con ID: {}", productGuardado.getId());

        return productMapper.toResponse(productGuardado);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest productRequest) {
        Product productExistente = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado ID: " + id));

        Category category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));

        if (Boolean.TRUE.equals(productRequest.featured()) && !productExistente.getFeatured()) {
            log.info("Producto {} marcado como destacado. Reseteando otros.", id);
            productRepository.resetFeatured();
        }

        productExistente.setNombre(productRequest.nombre());
        productExistente.setDescripcion(productRequest.descripcion());
        productExistente.setPrecio(productRequest.precio());
        productExistente.setDescuento(productRequest.descuento() != null ? productRequest.descuento() : 0);
        productExistente.setCategory(category);
        productExistente.setGenero(productRequest.genero());
        productExistente.setFeatured(productRequest.featured() != null ? productRequest.featured() : false);

        if (productRequest.brandId() != null && productRequest.brandId() > 0) {
            Brand brand = brandRepository.findById(productRequest.brandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Marca no encontrada"));
            productExistente.setBrand(brand);
        } else {
            productExistente.setBrand(null);
        }


        productExistente.getImages().clear();
        if (productRequest.images() != null) {
            for (String url : productRequest.images()) {
                if (url != null && !url.isBlank()) {
                    productExistente.addImage(new ProductImage(url));
                }
            }
        }


        productExistente.getVariants().clear();
        if (productRequest.variantes() != null) {
            for (ProductRequest.VariantRequest vReq : productRequest.variantes()) {
                ProductVariant variant = new ProductVariant();
                variant.setColor(vReq.color().toUpperCase());
                variant.setColorHex(vReq.colorHex());
                variant.setTalla(vReq.talla().toUpperCase());
                variant.setStock(vReq.stock());
                productExistente.addVariant(variant);
            }
        }

        Product productActualizado = productRepository.save(productExistente);
        log.info("Producto actualizado ID: {}", id);

        return productMapper.toResponse(productActualizado);
    }

    @Override
    public void deleteById(Long id) {
        if(!productRepository.existsById(id)){
            log.warn("Intento de eliminar producto inexistente ID: {}", id);
            throw new ResourceNotFoundException("Producto no encontrado con id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Producto eliminado ID: {}", id);
    }


}