package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoIva;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_compra", indexes = {
    @Index(name = "idx_detalle_compra", columnList = "compra_id"),
    @Index(name = "idx_detalle_producto", columnList = "producto_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    @JsonBackReference
    private Compra compra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 0)
    private BigDecimal costoUnitario;

    @Column(nullable = false, precision = 14, scale = 0)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva", nullable = false, length = 20)
    private TipoIva tipoIva = TipoIva.IVA_10;

    @Column(name = "monto_iva", precision = 12, scale = 0)
    private BigDecimal montoIva = BigDecimal.ZERO;

    public DetalleCompra() {
    }

    public DetalleCompra(Long id, Compra compra, Producto producto, BigDecimal cantidad,
                         BigDecimal costoUnitario, BigDecimal subtotal, TipoIva tipoIva, BigDecimal montoIva) {
        this.id = id;
        this.compra = compra;
        this.producto = producto;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.subtotal = subtotal;
        this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10;
        this.montoIva = montoIva != null ? montoIva : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; }

    public BigDecimal getMontoIva() { return montoIva; }
    public void setMontoIva(BigDecimal montoIva) { this.montoIva = montoIva; }
}
