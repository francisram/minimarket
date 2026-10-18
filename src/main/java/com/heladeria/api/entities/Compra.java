package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.heladeria.api.entities.enums.CondicionCompra;
import com.heladeria.api.entities.enums.EstadoCompra;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras", indexes = {
    @Index(name = "idx_compras_fecha", columnList = "fecha_emision"),
    @Index(name = "idx_compras_factura", columnList = "numero_factura"),
    @Index(name = "idx_compras_proveedor", columnList = "proveedor_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false, length = 50)
    private String numeroFactura;

    @Column(name = "timbrado", length = 20)
    private String timbrado;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_compra", nullable = false, length = 20)
    private CondicionCompra condicionCompra = CondicionCompra.CONTADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.REGISTRADA;

    @Column(name = "total_exenta", precision = 14, scale = 0)
    private BigDecimal totalExenta = BigDecimal.ZERO;

    @Column(name = "total_iva5", precision = 14, scale = 0)
    private BigDecimal totalIva5 = BigDecimal.ZERO;

    @Column(name = "total_iva10", precision = 14, scale = 0)
    private BigDecimal totalIva10 = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 14, scale = 0)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 255)
    private String observacion;

    @Column(length = 100)
    private String usuario;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<DetalleCompra> detalles = new ArrayList<>();

    public Compra() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoCompra.REGISTRADA;
    }

    public Compra(Long id, Proveedor proveedor, String numeroFactura, String timbrado,
                  LocalDate fechaEmision, LocalDate fechaVencimiento, CondicionCompra condicionCompra,
                  EstadoCompra estado, BigDecimal totalExenta, BigDecimal totalIva5,
                  BigDecimal totalIva10, BigDecimal total, String observacion, String usuario) {
        this.id = id;
        this.proveedor = proveedor;
        this.numeroFactura = numeroFactura;
        this.timbrado = timbrado;
        this.fechaEmision = fechaEmision != null ? fechaEmision : LocalDate.now();
        this.fechaVencimiento = fechaVencimiento;
        this.condicionCompra = condicionCompra != null ? condicionCompra : CondicionCompra.CONTADO;
        this.estado = estado != null ? estado : EstadoCompra.REGISTRADA;
        this.totalExenta = totalExenta != null ? totalExenta : BigDecimal.ZERO;
        this.totalIva5 = totalIva5 != null ? totalIva5 : BigDecimal.ZERO;
        this.totalIva10 = totalIva10 != null ? totalIva10 : BigDecimal.ZERO;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.observacion = observacion;
        this.usuario = usuario;
        this.fechaCreacion = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoCompra.REGISTRADA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public CondicionCompra getCondicionCompra() { return condicionCompra; }
    public void setCondicionCompra(CondicionCompra condicionCompra) { this.condicionCompra = condicionCompra; }

    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }

    public BigDecimal getTotalExenta() { return totalExenta; }
    public void setTotalExenta(BigDecimal totalExenta) { this.totalExenta = totalExenta; }

    public BigDecimal getTotalIva5() { return totalIva5; }
    public void setTotalIva5(BigDecimal totalIva5) { this.totalIva5 = totalIva5; }

    public BigDecimal getTotalIva10() { return totalIva10; }
    public void setTotalIva10(BigDecimal totalIva10) { this.totalIva10 = totalIva10; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<DetalleCompra> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompra> detalles) { this.detalles = detalles; }

    public void agregarDetalle(DetalleCompra detalle) {
        this.detalles.add(detalle);
        detalle.setCompra(this);
    }
}
