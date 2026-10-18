package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto_proveedores", uniqueConstraints = {
    @UniqueConstraint(name = "uq_producto_proveedor", columnNames = {"producto_id", "proveedor_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductoProveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonBackReference
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "codigo_referencia", length = 50)
    private String codigoReferencia;

    @Column(name = "costo_compra", precision = 12, scale = 0)
    private BigDecimal costoCompra;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal = false;

    @Column(name = "fecha_asociacion", nullable = false, updatable = false)
    private LocalDateTime fechaAsociacion;

    public ProductoProveedor() {
    }

    public ProductoProveedor(Producto producto, Proveedor proveedor, String codigoReferencia, BigDecimal costoCompra, Boolean esPrincipal) {
        this.producto = producto;
        this.proveedor = proveedor;
        this.codigoReferencia = codigoReferencia;
        this.costoCompra = costoCompra != null ? costoCompra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.esPrincipal = esPrincipal != null ? esPrincipal : false;
        this.fechaAsociacion = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.fechaAsociacion == null) {
            this.fechaAsociacion = LocalDateTime.now();
        }
        if (this.esPrincipal == null) {
            this.esPrincipal = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public String getCodigoReferencia() { return codigoReferencia; }
    public void setCodigoReferencia(String codigoReferencia) { this.codigoReferencia = codigoReferencia; }

    public BigDecimal getCostoCompra() { return costoCompra; }
    public void setCostoCompra(BigDecimal costoCompra) {
        this.costoCompra = costoCompra != null ? costoCompra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }

    public LocalDateTime getFechaAsociacion() { return fechaAsociacion; }
    public void setFechaAsociacion(LocalDateTime fechaAsociacion) { this.fechaAsociacion = fechaAsociacion; }
}
