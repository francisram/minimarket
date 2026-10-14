package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.TipoItemStock;
import java.math.BigDecimal;

public class ArticuloInventarioDTO {

    private Long id;
    private String nombre;
    private TipoItemStock tipoItem;
    private Double stockActual;
    private String unidad;
    private BigDecimal precio;

    public ArticuloInventarioDTO() {}

    public ArticuloInventarioDTO(Long id, String nombre, TipoItemStock tipoItem, Double stockActual, String unidad, BigDecimal precio) {
        this.id = id;
        this.nombre = nombre;
        this.tipoItem = tipoItem;
        this.stockActual = stockActual;
        this.unidad = unidad;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoItemStock getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemStock tipoItem) { this.tipoItem = tipoItem; }

    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}
