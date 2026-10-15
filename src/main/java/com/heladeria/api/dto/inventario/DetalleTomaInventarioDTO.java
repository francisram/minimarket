package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import java.math.BigDecimal;

public class DetalleTomaInventarioDTO {

    private Long id;
    private TipoItemStock tipoItem;
    private Long itemId;
    private String itemNombre;
    private String unidad;
    private Double stockSistema;
    private Double stockFisico;
    private Double diferencia;
    private BigDecimal costoUnitario;
    private BigDecimal diferenciaValorizada;

    public DetalleTomaInventarioDTO() {
    }

    public DetalleTomaInventarioDTO(Long id, TipoItemStock tipoItem, Long itemId, String itemNombre,
                                    String unidad, Double stockSistema, Double stockFisico, Double diferencia,
                                    BigDecimal costoUnitario, BigDecimal diferenciaValorizada) {
        this.id = id;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.itemNombre = itemNombre;
        this.unidad = unidad;
        this.stockSistema = stockSistema;
        this.stockFisico = stockFisico;
        this.diferencia = diferencia;
        this.costoUnitario = costoUnitario;
        this.diferenciaValorizada = diferenciaValorizada;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public Double getStockSistema() {
        return stockSistema;
    }

    public void setStockSistema(Double stockSistema) {
        this.stockSistema = stockSistema;
    }

    public Double getStockFisico() {
        return stockFisico;
    }

    public void setStockFisico(Double stockFisico) {
        this.stockFisico = stockFisico;
    }

    public Double getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(Double diferencia) {
        this.diferencia = diferencia;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BigDecimal getDiferenciaValorizada() {
        return diferenciaValorizada;
    }

    public void setDiferenciaValorizada(BigDecimal diferenciaValorizada) {
        this.diferenciaValorizada = diferenciaValorizada;
    }
}
