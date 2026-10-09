package com.heladeria.api.dto.stock;

import jakarta.validation.constraints.Min;

public class AjusteStockDTO {

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Min(value = 0, message = "El stock en kilos no puede ser negativo")
    private Double stockKilos;

    private Integer stockMinimo;

    private Double stockMinimoKilos;

    public AjusteStockDTO() {
    }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Double getStockKilos() { return stockKilos; }
    public void setStockKilos(Double stockKilos) { this.stockKilos = stockKilos; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Double getStockMinimoKilos() { return stockMinimoKilos; }
    public void setStockMinimoKilos(Double stockMinimoKilos) { this.stockMinimoKilos = stockMinimoKilos; }
}
