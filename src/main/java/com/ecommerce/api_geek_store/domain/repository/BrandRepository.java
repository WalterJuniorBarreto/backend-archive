package com.ecommerce.api_geek_store.domain.repository;


import com.ecommerce.api_geek_store.domain.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNombre(String nombre);
}