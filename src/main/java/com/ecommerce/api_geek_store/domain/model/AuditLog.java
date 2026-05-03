package com.ecommerce.api_geek_store.domain.model;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs",
indexes = {
        @Index(name = "idx_audit_empleado", columnList = "empleado_email"),
        @Index(name = "idx_audit_fecha", columnList = "fecha")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empleado_email", nullable = false)
    private String empleadoEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "empleado_rol", nullable = false, length = 20)
    private Role empleadoRol;

    @Column(nullable = false)
    private String accion;

    @Column(name = "usuario_afectado_id")
    private Long usuarioAfectadoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "usuario_afectado_rol", length = 20)
    private Role usuarioAfectadoRol;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLog)) return false;
        AuditLog auditLog = (AuditLog) o;
        return id != null && id.equals(auditLog.getId());
    }



    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
