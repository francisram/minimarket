package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock", indexes = {
    @Index(name = "idx_mov_fecha", columnList = "fecha"),
    @Index(name = "idx_mov_item", columnList = "tipo_item, item_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 30)
    private TipoItemStock tipoItem;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_nombre", nullable = false, length = 150)
    private String itemNombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private TipoMovimientoStock tipoMovimiento;

    @Column(nullable = false)
    private Double cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Double stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private Double stockPosterior;

    @Column(length = 255)
    private String motivo;

    @Column(length = 100)
    private String usuario;

    @Column(name = "costo_unitario", precision = 12, scale = 0)
    private BigDecimal costoUnitario;

    public MovimientoStock() {
    }

    public MovimientoStock(Long id, LocalDateTime fecha, TipoItemStock tipoItem, Long itemId, String itemNombre,
                           TipoMovimientoStock tipoMovimiento, Double cantidad, Double stockAnterior,
                           Double stockPosterior, String motivo, String usuario, BigDecimal costoUnitario) {
        this.id = id;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.itemNombre = itemNombre;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockPosterior = stockPosterior;
        this.motivo = motivo;
        this.usuario = usuario;
        this.costoUnitario = costoUnitario;
    }

    public static MovimientoStockBuilder builder() {
        return new MovimientoStockBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public TipoItemStock getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemStock tipoItem) { this.tipoItem = tipoItem; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemNombre() { return itemNombre; }
    public void setItemNombre(String itemNombre) { this.itemNombre = itemNombre; }

    public TipoMovimientoStock getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimientoStock tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public Double getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(Double stockAnterior) { this.stockAnterior = stockAnterior; }

    public Double getStockPosterior() { return stockPosterior; }
    public void setStockPosterior(Double stockPosterior) { this.stockPosterior = stockPosterior; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public static class MovimientoStockBuilder {
        private Long id;
        private LocalDateTime fecha = LocalDateTime.now();
        private TipoItemStock tipoItem;
        private Long itemId;
        private String itemNombre;
        private TipoMovimientoStock tipoMovimiento;
        private Double cantidad;
        private Double stockAnterior;
        private Double stockPosterior;
        private String motivo;
        private String usuario;
        private BigDecimal costoUnitario;

        public MovimientoStockBuilder id(Long id) { this.id = id; return this; }
        public MovimientoStockBuilder fecha(LocalDateTime fecha) { this.fecha = fecha; return this; }
        public MovimientoStockBuilder tipoItem(TipoItemStock tipoItem) { this.tipoItem = tipoItem; return this; }
        public MovimientoStockBuilder itemId(Long itemId) { this.itemId = itemId; return this; }
        public MovimientoStockBuilder itemNombre(String itemNombre) { this.itemNombre = itemNombre; return this; }
        public MovimientoStockBuilder tipoMovimiento(TipoMovimientoStock tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; return this; }
        public MovimientoStockBuilder cantidad(Double cantidad) { this.cantidad = cantidad; return this; }
        public MovimientoStockBuilder stockAnterior(Double stockAnterior) { this.stockAnterior = stockAnterior; return this; }
        public MovimientoStockBuilder stockPosterior(Double stockPosterior) { this.stockPosterior = stockPosterior; return this; }
        public MovimientoStockBuilder motivo(String motivo) { this.motivo = motivo; return this; }
        public MovimientoStockBuilder usuario(String usuario) { this.usuario = usuario; return this; }
        public MovimientoStockBuilder costoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; return this; }

        public MovimientoStock build() {
            return new MovimientoStock(id, fecha, tipoItem, itemId, itemNombre, tipoMovimiento, cantidad, stockAnterior, stockPosterior, motivo, usuario, costoUnitario);
        }
    }
}
