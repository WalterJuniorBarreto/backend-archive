package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.request.ProductVariantRequest;
import com.ecommerce.api_geek_store.api.dto.response.ProductVariantResponse;
import com.ecommerce.api_geek_store.api.mapper.ProductVariantMapper;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.ProductVariant;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductVariantRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantMapper productVariantMapper;
    private final ProductVariantRepository productVariantRepository;


    @Override
    @Transactional
    public ProductVariantResponse addVariantToProduct(Long productId, ProductVariantRequest request){
        log.info("Agregando variante al producto ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", productId)
                ));

        ProductVariant variant = productVariantMapper.toEntity(request);

        product.addVariant(variant);

        String sku = generateSku(product.getId(), request.color(), request.talla());
        variant.setSku(sku);

        ProductVariant savedVariant = productVariantRepository.save(variant);

        log.info("Variante creada con exito. SKU {}", savedVariant.getSku());
        return productVariantMapper.toResponse(savedVariant);
    }

    private String generateSku(Long productId, String color, String talla){
        String cleanColor = color.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        String shortColor = cleanColor.length() > 3 ? cleanColor.substring(0, 3) : cleanColor;
        String cleanTalla = talla.replaceAll("\\s+", "").toUpperCase();

        return String.format("PRD-%d-%s-%s", productId, shortColor, cleanTalla);
    }


    @Override
    @Transactional
    public ProductVariantResponse updateVariant(Long productId, Long variantId, ProductVariantRequest request){
        log.info("Actualizando variante ID {} del producto ID {}", variantId, productId);

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        String.format("Variante no encontrada con ID: %d", variantId)
                ));

        if(!variant.getProduct().getId().equals(productId)){
            log.error("Alerta de seguridad: La variante {} no pertenece al producto {}", variantId, productId);
            throw new RuntimeException("Conflicto de jerarquia: OPERACION DENEGADA");
        }


        productVariantMapper.updateEntityFromRequest(request, variant);

        ProductVariant updatedVariant = productVariantRepository.save(variant);

        log.info("Variante ID {} actualizada con exito", updatedVariant.getId());
        return productVariantMapper.toResponse(updatedVariant);
    }

}
