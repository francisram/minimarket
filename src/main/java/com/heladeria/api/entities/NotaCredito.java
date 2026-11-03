package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Nota de crédito: anula total o parcialmente una factura emitida. */
@Entity
@Table(name = "notas_credito")
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String numero;

    @Column(nullable = false, length = 30)
    private String timbrado;

    @Column(name = "timbrado_vencimiento")
    private LocalDate timbradoVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_expedicion_id")
    @JsonIgnore
    private PuntoExpedicion puntoExpedicion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(name = "reintegra_stock", nullable = false)
    private boolean reintegraStock;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(name = "autorizado_por", length = 50)
    private String autorizadoPor;

    @Column(name = "total_exentas", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalExentas = BigDecimal.ZERO;
    @Column(name = "total_gravada5", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalGravada5 = BigDecimal.ZERO;
    @Column(name = "total_gravada10", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalGravada10 = BigDecimal.ZERO;
    @Column(name = "total_iva5", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalIva5 = BigDecimal.ZERO;
    @Column(name = "total_iva10", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalIva10 = BigDecimal.ZERO;
    @Column(name = "total_iva", nullable = false, precision = 12, scale = 0)
    private BigDecimal totalIva = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "notaCredito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleNotaCredito> detalles = new ArrayList<>();

    public NotaCredito() { }

    public void agregarDetalle(DetalleNotaCredito d) {
        d.setNotaCredito(this);
        detalles.add(d);
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }
    public LocalDate getTimbradoVencimiento() { return timbradoVencimiento; }
    public void setTimbradoVencimiento(LocalDate v) { this.timbradoVencimiento = v; }
    public PuntoExpedicion getPuntoExpedicion() { return puntoExpedicion; }
    public void setPuntoExpedicion(PuntoExpedicion p) { this.puntoExpedicion = p; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public boolean isReintegraStock() { return reintegraStock; }
    public void setReintegraStock(boolean v) { this.reintegraStock = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(String v) { this.autorizadoPor = v; }
    public BigDecimal getTotalExentas() { return totalExentas; }
    public void setTotalExentas(BigDecimal v) { this.totalExentas = v; }
    public BigDecimal getTotalGravada5() { return totalGravada5; }
    public void setTotalGravada5(BigDecimal v) { this.totalGravada5 = v; }
    public BigDecimal getTotalGravada10() { return totalGravada10; }
    public void setTotalGravada10(BigDecimal v) { this.totalGravada10 = v; }
    public BigDecimal getTotalIva5() { return totalIva5; }
    public void setTotalIva5(BigDecimal v) { this.totalIva5 = v; }
    public BigDecimal getTotalIva10() { return totalIva10; }
    public void setTotalIva10(BigDecimal v) { this.totalIva10 = v; }
    public BigDecimal getTotalIva() { return totalIva; }
    public void setTotalIva(BigDecimal v) { this.totalIva = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { this.total = v; }
    public List<DetalleNotaCredito> getDetalles() { return detalles; }
}
