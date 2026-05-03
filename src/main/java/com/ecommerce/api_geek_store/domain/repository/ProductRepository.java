package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.Genero;
import com.ecommerce.api_geek_store.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    List<Product> findByCategoryId(Long categoryId);


    @Query(value = "SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
            "(:gender IS NULL OR p.genero = :gender) AND " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')))",


            countQuery = "SELECT count(p) FROM Product p WHERE " +
                    "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                    "(:brandId IS NULL OR p.brand.id = :brandId) AND " +
                    "(:gender IS NULL OR p.genero = :gender) AND " +
                    "(:keyword IS NULL OR :keyword = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> buscarConFiltros(
            @Param("categoryId") Long categoryId,
            @Param("brandId") Long brandId,
            @Param("gender") Genero gender,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<Product> findFirstByFeaturedTrue();


    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.featured = false")
    void resetFeatured();
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.variants " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.brand " +
            "WHERE p.id = :id")
    Optional<Product> findByIdWithRelations(@Param("id") Long id);

}