package com.heladeria.api.dto.reportes;

import java.math.BigDecimal;
import java.time.LocalDate;

public class VentaDiariaDTO {
    private LocalDate fecha;
    private BigDecimal total;
    private long cantidadPedidos;

    public VentaDiariaDTO() {
    }

    public VentaDiariaDTO(LocalDate fecha, BigDecimal total, long cantidadPedidos) {
        this.fecha = fecha;
        this.total = total;
        this.cantidadPedidos = cantidadPedidos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public long getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(long cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }
}
