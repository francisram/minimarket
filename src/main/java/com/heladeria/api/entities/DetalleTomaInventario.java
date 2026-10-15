package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoItemStock;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_toma_inventario", indexes = {
    @Index(name = "idx_dti_toma", columnList = "toma_inventario_id"),
    @Index(name = "idx_dti_item", columnList = "tipo_item, item_id")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DetalleTomaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toma_inventario_id", nullable = false)
    @JsonIgnore
    private TomaInventario tomaInventario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 30)
    private TipoItemStock tipoItem;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_nombre", nullable = false, length = 150)
    private String itemNombre;

    @Column(nullable = false, length = 20)
    private String unidad;

    @Column(name = "stock_sistema", nullable = false)
    private Double stockSistema;

    @Column(name = "stock_fisico", nullable = false)
    private Double stockFisico;

    @Column(nullable = false)
    private Double diferencia;

    @Column(name = "costo_unitario", precision = 12, scale = 0)
    private BigDecimal costoUnitario;

    @Column(name = "diferencia_valorizada", precision = 14, scale = 0)
    private BigDecimal diferenciaValorizada;

    public DetalleTomaInventario() {
    }

    public DetalleTomaInventario(Long id, TomaInventario tomaInventario, TipoItemStock tipoItem, Long itemId,
                                 String itemNombre, String unidad, Double stockSistema, Double stockFisico,
                                 Double diferencia, BigDecimal costoUnitario, BigDecimal diferenciaValorizada) {
        this.id = id;
        this.tomaInventario = tomaInventario;
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

    public TomaInventario getTomaInventario() {
        return tomaInventario;
    }

    public void setTomaInventario(TomaInventario tomaInventario) {
        this.tomaInventario = tomaInventario;
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
