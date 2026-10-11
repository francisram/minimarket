package com.heladeria.api.dto.reportes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardReporteDTO {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFin;

    private BigDecimal totalVentas = BigDecimal.ZERO;
    private long cantidadVentas = 0;
    private BigDecimal ticketPromedio = BigDecimal.ZERO;

    // Métodos de pago
    private BigDecimal totalEfectivo = BigDecimal.ZERO;
    private BigDecimal totalTarjetaDebito = BigDecimal.ZERO;
    private BigDecimal totalTarjetaCredito = BigDecimal.ZERO;
    private BigDecimal totalTransferenciaQr = BigDecimal.ZERO;

    // Comprobantes
    private long totalFacturas = 0;
    private long totalTickets = 0;

    // Fiscal IVA
    private BigDecimal totalExentas = BigDecimal.ZERO;
    private BigDecimal totalGravada5 = BigDecimal.ZERO;
    private BigDecimal totalIva5 = BigDecimal.ZERO;
    private BigDecimal totalGravada10 = BigDecimal.ZERO;
    private BigDecimal totalIva10 = BigDecimal.ZERO;
    private BigDecimal totalIva = BigDecimal.ZERO;

    // Tops y gráficos
    private List<ItemRankingDTO> topProductos = new ArrayList<>();
    private List<SaborRankingDTO> topSabores = new ArrayList<>();
    private List<VentaDiariaDTO> ventasPorDia = new ArrayList<>();

    public DashboardReporteDTO() {
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

    public BigDecimal getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(BigDecimal ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public BigDecimal getTotalEfectivo() {
        return totalEfectivo;
    }

    public void setTotalEfectivo(BigDecimal totalEfectivo) {
        this.totalEfectivo = totalEfectivo;
    }

    public BigDecimal getTotalTarjetaDebito() {
        return totalTarjetaDebito;
    }

    public void setTotalTarjetaDebito(BigDecimal totalTarjetaDebito) {
        this.totalTarjetaDebito = totalTarjetaDebito;
    }

    public BigDecimal getTotalTarjetaCredito() {
        return totalTarjetaCredito;
    }

    public void setTotalTarjetaCredito(BigDecimal totalTarjetaCredito) {
        this.totalTarjetaCredito = totalTarjetaCredito;
    }

    public BigDecimal getTotalTransferenciaQr() {
        return totalTransferenciaQr;
    }

    public void setTotalTransferenciaQr(BigDecimal totalTransferenciaQr) {
        this.totalTransferenciaQr = totalTransferenciaQr;
    }

    public long getTotalFacturas() {
        return totalFacturas;
    }

    public void setTotalFacturas(long totalFacturas) {
        this.totalFacturas = totalFacturas;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
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

    public List<ItemRankingDTO> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<ItemRankingDTO> topProductos) {
        this.topProductos = topProductos;
    }

    public List<SaborRankingDTO> getTopSabores() {
        return topSabores;
    }

    public void setTopSabores(List<SaborRankingDTO> topSabores) {
        this.topSabores = topSabores;
    }

    public List<VentaDiariaDTO> getVentasPorDia() {
        return ventasPorDia;
    }

    public void setVentasPorDia(List<VentaDiariaDTO> ventasPorDia) {
        this.ventasPorDia = ventasPorDia;
    }
}
