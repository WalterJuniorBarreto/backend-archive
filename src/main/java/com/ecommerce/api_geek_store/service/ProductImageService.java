package com.ecommerce.api_geek_store.service;

import com.ecommerce.api_geek_store.api.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ProductImageService {
    ProductImageResponse uploadImage(Long productId, MultipartFile file, Integer orden);
    void deleteImage(Long productId, Long imageId);
}
