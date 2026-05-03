package com.ecommerce.api_geek_store.domain.repository;

import com.ecommerce.api_geek_store.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Para que el Admin filtre las acciones de un empleado en específico
    Page<AuditLog> findByEmpleadoEmail(String empleadoEmail, Pageable pageable);

    // Para ver todo el historial de lo que le han hecho a un cliente en específico
    Page<AuditLog> findByUsuarioAfectadoId(Long usuarioAfectadoId, Pageable pageable);
}