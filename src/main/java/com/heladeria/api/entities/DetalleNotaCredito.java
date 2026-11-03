package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heladeria.api.entities.enums.TipoIva;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalles_nota_credito")
public class DetalleNotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nota_credito_id", nullable = false)
    @JsonIgnore
    private NotaCredito notaCredito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_pedido_id", nullable = false)
    @JsonIgnore
    private DetallePedido detallePedido;

    @Column(nullable = false, length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 0)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva", nullable = false, length = 20)
    private TipoIva tipoIva;

    public DetalleNotaCredito() { }

    public Long getId() { return id; }
    public NotaCredito getNotaCredito() { return notaCredito; }
    public void setNotaCredito(NotaCredito n) { this.notaCredito = n; }
    public DetallePedido getDetallePedido() { return detallePedido; }
    public void setDetallePedido(DetallePedido d) { this.detallePedido = d; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer c) { this.cantidad = c; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p) { this.precioUnitario = p; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal s) { this.subtotal = s; }
    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva t) { this.tipoIva = t; }
}
