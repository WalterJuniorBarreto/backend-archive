package com.ecommerce.api_geek_store.domain.repository;


import com.ecommerce.api_geek_store.domain.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    @Query("SELECT b FROM Brand b WHERE " +
            "(:searchTerm IS NULL OR :searchTerm = '' OR LOWER(b.nombre) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "AND (:statusFilter = 'TODOS' " +
            "OR (:statusFilter = 'ACTIVOS' AND b.activo = true) " +
            "OR (:statusFilter = 'INACTIVOS' AND b.activo = false))")
    Page<Brand> findWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("statusFilter") String statusFilter,
            Pageable pageable
    );
}
