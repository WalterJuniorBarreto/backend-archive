package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
