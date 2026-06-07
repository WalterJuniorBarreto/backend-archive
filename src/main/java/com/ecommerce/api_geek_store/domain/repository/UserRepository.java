package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.rol = 'ROLE_USER' AND (" +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchOnlyCustomers(@Param("keyword") String keyword, Pageable pageable);

    Page<User> findByRol(String rol, Pageable pageable);

}
