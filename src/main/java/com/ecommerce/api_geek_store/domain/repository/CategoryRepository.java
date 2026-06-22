package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.Category;
import com.ecommerce.api_geek_store.domain.model.enums.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.CancellationException;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

       boolean existsByNombreIgnoreCase(String nombre);

       @Query("SELECT c FROM Category c WHERE " +
                "(:status IS NULL OR c.status = :status) AND " +
               "(:searchTerm IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')))")
       Page<Category> findCategoriesWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("status") CategoryStatus status,
            Pageable pageable
       );

}