package com.heladeria.api.dto.reportes;

import com.heladeria.api.entities.enums.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteVentasDTO {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFin;
    private BigDecimal totalVentas = BigDecimal.ZERO;
    private long cantidadVentas = 0;
    private Map<MetodoPago, BigDecimal> resumenMetodosPago = new HashMap<>();
    private List<PedidoResumenReporteDTO> pedidos = new ArrayList<>();

    public ReporteVentasDTO() {
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

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public long getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(long cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Map<MetodoPago, BigDecimal> getResumenMetodosPago() {
        return resumenMetodosPago;
    }

    public void setResumenMetodosPago(Map<MetodoPago, BigDecimal> resumenMetodosPago) {
        this.resumenMetodosPago = resumenMetodosPago;
    }

    public List<PedidoResumenReporteDTO> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoResumenReporteDTO> pedidos) {
        this.pedidos = pedidos;
    }
}
