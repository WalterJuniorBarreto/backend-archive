package com.ecommerce.api_geek_store.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "orders",

        indexes = {
                @Index(name = "idx_order_tracking", columnList = "trackingNumber"),
                @Index(name = "idx_order_user", columnList = "user_id"),
                @Index(name = "idx_order_fecha", columnList = "fecha_creacion")
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @Column(length = 50)
    private String trackingNumber;

    @Column(length = 100)
    private String courierName;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "cod_operacion", length = 50)
    private String codOperacion;

    @Column(name = "url_comprobante")
    private String urlComprobante;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "envio_calle")),
            @AttributeOverride(name = "ciudad", column = @Column(name = "envio_ciudad")),
            @AttributeOverride(name = "estado", column = @Column(name = "envio_estado")),
            @AttributeOverride(name = "codigoPostal", column = @Column(name = "envio_cp")),
            @AttributeOverride(name = "pais", column = @Column(name = "envio_pais"))
    })
    private ShippingAddress envio;




    @PrePersist
    protected void onCreate(){
        fechaCreacion = LocalDateTime.now();
        if(estado == null) estado = "PENDIENTE";
    }
    public Order(){}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<OrderItem> getItems() { return items; }
    public void setItems(Set<OrderItem> items) { this.items = items; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getCodOperacion() { return codOperacion; }
    public void setCodOperacion(String codOperacion) { this.codOperacion = codOperacion; }

    public String getUrlComprobante() { return urlComprobante; }
    public void setUrlComprobante(String urlComprobante) { this.urlComprobante = urlComprobante; }

    public ShippingAddress getEnvio() { return envio; }
    public void setEnvio(ShippingAddress envio) { this.envio = envio; }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }
}