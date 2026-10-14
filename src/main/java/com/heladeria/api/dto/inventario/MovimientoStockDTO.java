package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoStockDTO {

    private Long id;
    private LocalDateTime fecha;
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
    private String unidad;

    public MovimientoStockDTO() {}

    public MovimientoStockDTO(Long id, LocalDateTime fecha, TipoItemStock tipoItem, Long itemId, String itemNombre,
                              TipoMovimientoStock tipoMovimiento, Double cantidad, Double stockAnterior,
                              Double stockPosterior, String motivo, String usuario, BigDecimal costoUnitario, String unidad) {
        this.id = id;
        this.fecha = fecha;
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
        this.unidad = unidad;
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

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
}
