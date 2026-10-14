package com.heladeria.api.dto.inventario;

import java.math.BigDecimal;

public class KpisInventarioDTO {

    private double totalEntradas;
    private double totalSalidasVenta;
    private double totalMermas;
    private double totalAjustes;
    private long totalMovimientos;
    private BigDecimal valorTotalStock;

    public KpisInventarioDTO() {}

    public KpisInventarioDTO(double totalEntradas, double totalSalidasVenta, double totalMermas, double totalAjustes, long totalMovimientos, BigDecimal valorTotalStock) {
        this.totalEntradas = totalEntradas;
        this.totalSalidasVenta = totalSalidasVenta;
        this.totalMermas = totalMermas;
        this.totalAjustes = totalAjustes;
        this.totalMovimientos = totalMovimientos;
        this.valorTotalStock = valorTotalStock;
    }

    public double getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(double totalEntradas) { this.totalEntradas = totalEntradas; }

    public double getTotalSalidasVenta() { return totalSalidasVenta; }
    public void setTotalSalidasVenta(double totalSalidasVenta) { this.totalSalidasVenta = totalSalidasVenta; }

    public double getTotalMermas() { return totalMermas; }
    public void setTotalMermas(double totalMermas) { this.totalMermas = totalMermas; }

    public double getTotalAjustes() { return totalAjustes; }
    public void setTotalAjustes(double totalAjustes) { this.totalAjustes = totalAjustes; }

    public long getTotalMovimientos() { return totalMovimientos; }
    public void setTotalMovimientos(long totalMovimientos) { this.totalMovimientos = totalMovimientos; }

    public BigDecimal getValorTotalStock() { return valorTotalStock; }
    public void setValorTotalStock(BigDecimal valorTotalStock) { this.valorTotalStock = valorTotalStock; }
}
