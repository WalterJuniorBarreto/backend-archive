package com.ecommerce.api_geek_store.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones",
        indexes = {
                @Index(name = "idx_reclamo_codigo", columnList = "codigoReclamacion", unique = true),
                @Index(name = "idx_reclamo_dni", columnList = "dni"),
                @Index(name = "idx_reclamo_resuelto", columnList = "resuelto")
        }
)
public class Reclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigoReclamacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;


    private LocalDateTime fechaResolucion;

    @Column(nullable = false, length = 150)
    private String nombreCompleto;

    @Column(length = 20)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false)
    private String email;

    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoBien tipoBien;


    private BigDecimal montoReclamado;

    @Column(length = 255)
    private String descripcionBien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReclamo tipoReclamo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String detalleProblema;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String pedidoConsumidor;

    @Column(nullable = false)
    private Boolean resuelto = false;

    public Reclamacion() {}

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.resuelto == null) this.resuelto = false;
    }

    public void setResuelto(Boolean resuelto) {
        this.resuelto = resuelto;
        if (Boolean.TRUE.equals(resuelto)) {
            this.fechaResolucion = LocalDateTime.now();
        } else {
            this.fechaResolucion = null;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoReclamacion() { return codigoReclamacion; }
    public void setCodigoReclamacion(String codigoReclamacion) { this.codigoReclamacion = codigoReclamacion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaResolucion() { return fechaResolucion; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public TipoBien getTipoBien() { return tipoBien; }
    public void setTipoBien(TipoBien tipoBien) { this.tipoBien = tipoBien; }

    public BigDecimal getMontoReclamado() { return montoReclamado; }
    public void setMontoReclamado(BigDecimal montoReclamado) { this.montoReclamado = montoReclamado; }

    public String getDescripcionBien() { return descripcionBien; }
    public void setDescripcionBien(String descripcionBien) { this.descripcionBien = descripcionBien; }

    public TipoReclamo getTipoReclamo() { return tipoReclamo; }
    public void setTipoReclamo(TipoReclamo tipoReclamo) { this.tipoReclamo = tipoReclamo; }

    public String getDetalleProblema() { return detalleProblema; }
    public void setDetalleProblema(String detalleProblema) { this.detalleProblema = detalleProblema; }

    public String getPedidoConsumidor() { return pedidoConsumidor; }
    public void setPedidoConsumidor(String pedidoConsumidor) { this.pedidoConsumidor = pedidoConsumidor; }

    public Boolean getResuelto() { return resuelto; }
}