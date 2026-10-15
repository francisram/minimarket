package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemConteoDTO {

    @NotNull(message = "El tipo de ítem es obligatorio")
    private TipoItemStock tipoItem;

    @NotNull(message = "El ID del artículo es obligatorio")
    private Long itemId;

    private String itemNombre;

    @NotNull(message = "El stock físico contado es obligatorio")
    private Double stockFisico;

    private BigDecimal costoUnitario;

    public ItemConteoDTO() {
    }

    public ItemConteoDTO(TipoItemStock tipoItem, Long itemId, String itemNombre, Double stockFisico, BigDecimal costoUnitario) {
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.itemNombre = itemNombre;
        this.stockFisico = stockFisico;
        this.costoUnitario = costoUnitario;
    }

    public TipoItemStock getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(TipoItemStock tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemNombre() {
        return itemNombre;
    }

    public void setItemNombre(String itemNombre) {
        this.itemNombre = itemNombre;
    }

    public Double getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(Double stockFisico) {
        this.stockFisico = stockFisico;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }
}
