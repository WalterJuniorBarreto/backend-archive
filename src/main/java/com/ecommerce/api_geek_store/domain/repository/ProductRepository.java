package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    /**
     * @EntityGraph resuelve el problema N+1 haciendo un LEFT OUTER JOIN
     * automático con Category y Brand en una sola consulta SQL.
     */
    @Override
    @EntityGraph(attributePaths = {"category", "brand"})
    Page<Product> findAll(@Nullable Specification<Product> spec, Pageable pageable);

    /**
     * Búsqueda optimizada para la página de detalle del producto (SEO Friendly).
     * Trae de golpe toda la data necesaria para mostrar el producto.
     */
    @EntityGraph(attributePaths = {"category", "brand", "images", "variants"})
    Optional<Product> findBySlug(String slug);

    /**
     * Verificación de existencia rápida (Devuelve un boolean, no carga el objeto en memoria).
     * Ideal para validaciones de ciberseguridad antes de crear o actualizar.
     */
    boolean existsBySlug(String slug);

    boolean existsByNombre(String nombre);
}
