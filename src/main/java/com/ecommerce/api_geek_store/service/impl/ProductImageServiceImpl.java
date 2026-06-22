package com.ecommerce.api_geek_store.service.impl;

import com.ecommerce.api_geek_store.api.dto.response.ProductImageResponse;
import com.ecommerce.api_geek_store.api.mapper.ProductImageMapper;
import com.ecommerce.api_geek_store.domain.model.Product;
import com.ecommerce.api_geek_store.domain.model.ProductImage;
import com.ecommerce.api_geek_store.domain.repository.ProductImageRepository;
import com.ecommerce.api_geek_store.domain.repository.ProductRepository;
import com.ecommerce.api_geek_store.exception.ResourceNotFoundException;
import com.ecommerce.api_geek_store.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper imageMapper;

    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    @Override
    @Transactional
    public ProductImageResponse uploadImage(Long productId, MultipartFile file, Integer orden){
        log.info("Iniciando subida de imagen para el producto ID: {}", productId);

        //validaciones
        if(file == null || file.isEmpty()){
            throw new IllegalArgumentException("El archivo no puede estar vacio");
        }
        if(!ALLOWED_MIME_TYPES.contains(file.getContentType())){
            log.warn("Intento de subida de archivo malicioso detectado. Formato: {}", file.getContentType());
            throw new IllegalArgumentException("Formato no permitido. SOlo se acepta jpeg, png, webp");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Producto no encontrado con ID: %d", productId)
                ));

        //mvp - simulacion de aws s3
        //aqui llamaremos a storageService.uploadFIle(file)
        log.info("Generando URL placeholder temporal para evitar bloqueos de AWS");
        String temporalUrl = String.format("https://via.placeholder.com/800x800.png?text=Zapatilla+ID+%d+Vista+%d", product, orden);

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(temporalUrl)
                .orden(orden)
                .build();

        ProductImage savedImage = productImageRepository.save(image);

        log.info("Imagen guardada en BD con exito. ID: {}", savedImage.getId());

        return imageMapper.toResponse(savedImage);
    }


    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        log.info("Solicitud para eliminar imagen ID {} del producto ID {}", imageId, productId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Imagen no encontrada con ID %d", imageId)
                ));

        if(!image.getProduct().equals(productId)){
            log.error("Violacion de seguridad: La imagen {} no pertece al producto {}", imageId, productId);
            throw new RuntimeException("Operacion denegada: Conlifcto de pertenencia de imagen");
        }

        //storageService.deleteFile(image.getUrl())
        log.info("SImulando eliminacion del archivo en Storage para la URL: {}", image.getUrl());

        productImageRepository.delete(image);

        log.info("Imagen ID: {} eliminada exitosamente de la base de datos", imageId);
    }

}
