package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import java.math.BigDecimal;

public class ValorizacionItemDTO {

    private Long id;
    private String nombre;
    private TipoItemStock tipoItem;
    private Double stockActual;
    private Double stockMinimo;
    private String unidad;
    private BigDecimal precioUnitario;
    private BigDecimal totalValorizado;
    private String estadoStock;

    public ValorizacionItemDTO() {}

    public ValorizacionItemDTO(Long id, String nombre, TipoItemStock tipoItem, Double stockActual,
                               Double stockMinimo, String unidad, BigDecimal precioUnitario,
                               BigDecimal totalValorizado, String estadoStock) {
        this.id = id;
        this.nombre = nombre;
        this.tipoItem = tipoItem;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.unidad = unidad;
        this.precioUnitario = precioUnitario;
        this.totalValorizado = totalValorizado;
        this.estadoStock = estadoStock;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoItemStock getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemStock tipoItem) { this.tipoItem = tipoItem; }

    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }

    public Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getTotalValorizado() { return totalValorizado; }
    public void setTotalValorizado(BigDecimal totalValorizado) { this.totalValorizado = totalValorizado; }

    public String getEstadoStock() { return estadoStock; }
    public void setEstadoStock(String estadoStock) { this.estadoStock = estadoStock; }
}
