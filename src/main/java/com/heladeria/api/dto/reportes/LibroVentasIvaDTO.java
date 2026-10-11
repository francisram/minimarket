package com.heladeria.api.dto.reportes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LibroVentasIvaDTO {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFin;

    private BigDecimal totalExentas = BigDecimal.ZERO;
    private BigDecimal totalGravada5 = BigDecimal.ZERO;
    private BigDecimal totalIva5 = BigDecimal.ZERO;
    private BigDecimal totalGravada10 = BigDecimal.ZERO;
    private BigDecimal totalIva10 = BigDecimal.ZERO;
    private BigDecimal totalIva = BigDecimal.ZERO;
    private BigDecimal granTotal = BigDecimal.ZERO;
    private long cantidadComprobantes = 0;

    private List<ItemLibroIvaDTO> items = new ArrayList<>();

    public LibroVentasIvaDTO() {
    }

    public LocalDateTime getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(LocalDateTime periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    public LocalDateTime getPeriodoFin() {
        return periodoFin;
    }

    public void setPeriodoFin(LocalDateTime periodoFin) {
        this.periodoFin = periodoFin;
    }

    public BigDecimal getTotalExentas() {
        return totalExentas;
    }

    public void setTotalExentas(BigDecimal totalExentas) {
        this.totalExentas = totalExentas;
    }

    public BigDecimal getTotalGravada5() {
        return totalGravada5;
    }

    public void setTotalGravada5(BigDecimal totalGravada5) {
        this.totalGravada5 = totalGravada5;
    }

    public BigDecimal getTotalIva5() {
        return totalIva5;
    }

    public void setTotalIva5(BigDecimal totalIva5) {
        this.totalIva5 = totalIva5;
    }

    public BigDecimal getTotalGravada10() {
        return totalGravada10;
    }

    public void setTotalGravada10(BigDecimal totalGravada10) {
        this.totalGravada10 = totalGravada10;
    }

    public BigDecimal getTotalIva10() {
        return totalIva10;
    }

    public void setTotalIva10(BigDecimal totalIva10) {
        this.totalIva10 = totalIva10;
    }

    public BigDecimal getTotalIva() {
        return totalIva;
    }

    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva;
    }

    public BigDecimal getGranTotal() {
        return granTotal;
    }

    public void setGranTotal(BigDecimal granTotal) {
        this.granTotal = granTotal;
    }

    public long getCantidadComprobantes() {
        return cantidadComprobantes;
    }

    public void setCantidadComprobantes(long cantidadComprobantes) {
        this.cantidadComprobantes = cantidadComprobantes;
    }

    public List<ItemLibroIvaDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemLibroIvaDTO> items) {
        this.items = items;
    }
}
