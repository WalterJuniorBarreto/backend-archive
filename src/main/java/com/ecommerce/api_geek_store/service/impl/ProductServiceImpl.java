package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.request.ProductRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductResponse;
import com.ecommerce.api_geek_store.api.dto.request.ProductSearchCriteria;
import com.ecommerce.api_geek_store.api.mapper.ProductMapper;
import com.ecommerce.api_geek_store.domain.model.*;
import com.ecommerce.api_geek_store.domain.model.enums.ProductStatus;
import com.ecommerce.api_geek_store.domain.repository.BrandRepository;
import com.ecommerce.api_geek_store.domain.repository.CategoryRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.exception.DuplicateResourceException;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.ProductService;
import com.ecommerce.api_geek_store.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> findAllWithFilters(ProductSearchCriteria criteria, Pageable pageable) {

        log.debug("Iniciando busqueda de productos con filtros: {}", criteria);

        Specification<Product> spec = Specification.allOf(ProductSpecification.withStatus(criteria.status()))
                .and(ProductSpecification.withSearchTerm(criteria.searchTerm()))
                .and(ProductSpecification.withPriceRange(criteria.minPrice(), criteria.maxPrice()))
                .and(ProductSpecification.withCategories(criteria.categoryIds()))
                .and(ProductSpecification.withBrands(criteria.brandIds()))
                .and(ProductSpecification.withGender(criteria.gender()));

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        log.debug("Busqueda completada. Productos encontrados: {}", productPage.getTotalElements());

        return productPage.map(productMapper::toResponse);
    }

    @Override
    public ProductResponse getProductById(Long id){
        log.debug("Admin consultando detalle del producto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", id)
                ));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creando nuevo producto: {}", request.nombre());

        if(productRepository.existsByNombre(request.nombre())){
            throw new DuplicateResourceException(
                    String.format("Ya existe un producto con el nombre: %s", request.nombre())
            );
        }

        Product product = productMapper.toEntity(request);

        product.setSlug(generateSlug(request.nombre()));

        product.setStatus(ProductStatus.DRAFT);

        product.setCategory(categoryRepository.getReferenceById(request.categoryId()));
        product.setBrand(brandRepository.getReferenceById(request.brandId()));

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request){
        log.info("Iniciando actualizacion del producto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("PRoducto no encontrado con ID: %d", id)
                ));

        if(!product.getNombre().equalsIgnoreCase(request.nombre()) &&
            productRepository.existsByNombre(request.nombre())){
            log.warn("Conflicto al actualizar el nombre {} ya esta en uso", request.nombre());
            throw new DuplicateResourceException(
                    String.format("Ya existe otro producto con el nombre: %s", request.nombre())
            );
        }

        if(!product.getNombre().equals(request.nombre())){
            product.setSlug(generateSlug(request.nombre()));
        }

        productMapper.updateEntityFromRequest(request, product);

        product.setCategory(categoryRepository.getReferenceById(request.categoryId()));
        product.setBrand(brandRepository.getReferenceById(request.brandId()));

        Product updatedProduct = productRepository.save(product);

        log.info("PRoducto ID: {} actualizado exitosamente", updatedProduct.getId());
        return productMapper.toResponse(updatedProduct);
    }


    @Override
    @Transactional
    public void archiveProduct(Long id){
        log.info("Iniciando Soft Delete para el producto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", id)
                ));

        if(product.getStatus() == ProductStatus.ARCHIVED){
            log.info("El producto ID: {} ya se encuentra archivado. Abortando transaccion", id);
            return;
        }

        product.setStatus(ProductStatus.ARCHIVED);

        productRepository.save(product);

        log.info("Producto ID {} archivado exitosamente", id);
    }


    @Override
    @Transactional
    public void publishProduct(Long id){
        log.info("Iniciando proceso de publicacion para el producto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", id)
                ));

        if(product.getStatus() == ProductStatus.ACTIVE){
            log.info("El producto ID: {} ya esta ACTIVE. Abortando transaccion", id);
            return;
        }

        if(product.getVariants() == null || product.getVariants().isEmpty()){
            log.error("Intento de publicar producto ID: {} sin stock configurado", id);
            throw new IllegalStateException("No se puede publicar un producto que no tiene tallas ni stock asignado");
        }

        product.setStatus(ProductStatus.ACTIVE);

        productRepository.save(product);

        log.info("Producto ID: {} publicado y listo para la venta", id);
    }


    @Override
    @Transactional
    public void deactivateProduct(Long id){
        log.info("Iniciando desactivacion para el producto ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", id)
                ));

        if(product.getStatus() == ProductStatus.INACTIVE){
            log.info("El producto ID {} ya se encuentra INACTIVE. Aboratando transaccion", id);
            return;
        }

        if(product.getStatus() == ProductStatus.ARCHIVED){
            log.error("Intento invalido: No se puede cambiar INACTIVE un producto que ya esta ARCHIVED, ID {}", id);
            throw new IllegalStateException("El producto esta archivado y no se puede ser modificado a inactivo");
        }

        product.setStatus(ProductStatus.INACTIVE);

        productRepository.save(product);

        log.info("Producto ID: {} desactivado exitosamente", id);

    }










    private String generateSlug(String input){
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\p{ASCII}]", "");
        return slug.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9-]", "");
    }




}