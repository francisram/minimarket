package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RegistrarMovimientoDTO {

    @NotNull(message = "El tipo de artículo es obligatorio")
    private TipoItemStock tipoItem;

    @NotNull(message = "El id del artículo es obligatorio")
    private Long itemId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimientoStock tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    private String motivo;
    private BigDecimal costoUnitario;

    public RegistrarMovimientoDTO() {}

    public RegistrarMovimientoDTO(TipoItemStock tipoItem, Long itemId, TipoMovimientoStock tipoMovimiento, Double cantidad, String motivo, BigDecimal costoUnitario) {
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.costoUnitario = costoUnitario;
    }

    public TipoItemStock getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemStock tipoItem) { this.tipoItem = tipoItem; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public TipoMovimientoStock getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimientoStock tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }
}
