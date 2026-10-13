package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResumenCajaDTO {

    private Long sesionId;
    private EstadoSesionCaja estado;
    private String usuarioApertura;
    private LocalDateTime fechaApertura;
    private BigDecimal montoInicial;
    private BigDecimal totalEfectivo;
    private BigDecimal totalTarjetaDebito;
    private BigDecimal totalTarjetaCredito;
    private BigDecimal totalTransferenciaQr;
    private BigDecimal totalVentas;
    private BigDecimal totalEsperadoEfectivo;
    private BigDecimal totalEsperadoTarjeta;
    private Integer totalPedidos;
    private Integer cantidadVouchers;
    private Integer cantidadTransferencias;

    public ResumenCajaDTO() {
    }

    public ResumenCajaDTO(Long sesionId, EstadoSesionCaja estado, String usuarioApertura,
                          LocalDateTime fechaApertura, BigDecimal montoInicial, BigDecimal totalEfectivo,
                          BigDecimal totalTarjetaDebito, BigDecimal totalTarjetaCredito,
                          BigDecimal totalTransferenciaQr, BigDecimal totalVentas,
                          BigDecimal totalEsperadoEfectivo, Integer totalPedidos) {
        this(sesionId, estado, usuarioApertura, fechaApertura, montoInicial, totalEfectivo,
                totalTarjetaDebito, totalTarjetaCredito, totalTransferenciaQr, totalVentas,
                totalEsperadoEfectivo, null, totalPedidos, 0, 0);
    }

    public ResumenCajaDTO(Long sesionId, EstadoSesionCaja estado, String usuarioApertura,
                          LocalDateTime fechaApertura, BigDecimal montoInicial, BigDecimal totalEfectivo,
                          BigDecimal totalTarjetaDebito, BigDecimal totalTarjetaCredito,
                          BigDecimal totalTransferenciaQr, BigDecimal totalVentas,
                          BigDecimal totalEsperadoEfectivo, BigDecimal totalEsperadoTarjeta,
                          Integer totalPedidos, Integer cantidadVouchers, Integer cantidadTransferencias) {
        this.sesionId = sesionId;
        this.estado = estado;
        this.usuarioApertura = usuarioApertura;
        this.fechaApertura = fechaApertura;
        this.montoInicial = montoInicial;
        this.totalEfectivo = totalEfectivo;
        this.totalTarjetaDebito = totalTarjetaDebito;
        this.totalTarjetaCredito = totalTarjetaCredito;
        this.totalTransferenciaQr = totalTransferenciaQr;
        this.totalVentas = totalVentas;
        this.totalEsperadoEfectivo = totalEsperadoEfectivo;
        this.totalEsperadoTarjeta = totalEsperadoTarjeta;
        this.totalPedidos = totalPedidos;
        this.cantidadVouchers = cantidadVouchers;
        this.cantidadTransferencias = cantidadTransferencias;
    }

    public static ResumenCajaDTOBuilder builder() {
        return new ResumenCajaDTOBuilder();
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public EstadoSesionCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesionCaja estado) {
        this.estado = estado;
    }

    public String getUsuarioApertura() {
        return usuarioApertura;
    }

    public void setUsuarioApertura(String usuarioApertura) {
        this.usuarioApertura = usuarioApertura;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
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

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getTotalEsperadoEfectivo() {
        return totalEsperadoEfectivo;
    }

    public void setTotalEsperadoEfectivo(BigDecimal totalEsperadoEfectivo) {
        this.totalEsperadoEfectivo = totalEsperadoEfectivo;
    }

    public BigDecimal getTotalEsperadoTarjeta() {
        return totalEsperadoTarjeta;
    }

    public void setTotalEsperadoTarjeta(BigDecimal totalEsperadoTarjeta) {
        this.totalEsperadoTarjeta = totalEsperadoTarjeta;
    }

    public Integer getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Integer totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public Integer getCantidadVouchers() {
        return cantidadVouchers;
    }

    public void setCantidadVouchers(Integer cantidadVouchers) {
        this.cantidadVouchers = cantidadVouchers;
    }

    public Integer getCantidadTransferencias() {
        return cantidadTransferencias;
    }

    public void setCantidadTransferencias(Integer cantidadTransferencias) {
        this.cantidadTransferencias = cantidadTransferencias;
    }

    public static class ResumenCajaDTOBuilder {
        private Long sesionId;
        private EstadoSesionCaja estado;
        private String usuarioApertura;
        private LocalDateTime fechaApertura;
        private BigDecimal montoInicial;
        private BigDecimal totalEfectivo;
        private BigDecimal totalTarjetaDebito;
        private BigDecimal totalTarjetaCredito;
        private BigDecimal totalTransferenciaQr;
        private BigDecimal totalVentas;
        private BigDecimal totalEsperadoEfectivo;
        private BigDecimal totalEsperadoTarjeta;
        private Integer totalPedidos;
        private Integer cantidadVouchers;
        private Integer cantidadTransferencias;

        public ResumenCajaDTOBuilder sesionId(Long sesionId) { this.sesionId = sesionId; return this; }
        public ResumenCajaDTOBuilder estado(EstadoSesionCaja estado) { this.estado = estado; return this; }
        public ResumenCajaDTOBuilder usuarioApertura(String usuarioApertura) { this.usuarioApertura = usuarioApertura; return this; }
        public ResumenCajaDTOBuilder fechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; return this; }
        public ResumenCajaDTOBuilder montoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; return this; }
        public ResumenCajaDTOBuilder totalEfectivo(BigDecimal totalEfectivo) { this.totalEfectivo = totalEfectivo; return this; }
        public ResumenCajaDTOBuilder totalTarjetaDebito(BigDecimal totalTarjetaDebito) { this.totalTarjetaDebito = totalTarjetaDebito; return this; }
        public ResumenCajaDTOBuilder totalTarjetaCredito(BigDecimal totalTarjetaCredito) { this.totalTarjetaCredito = totalTarjetaCredito; return this; }
        public ResumenCajaDTOBuilder totalTransferenciaQr(BigDecimal totalTransferenciaQr) { this.totalTransferenciaQr = totalTransferenciaQr; return this; }
        public ResumenCajaDTOBuilder totalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; return this; }
        public ResumenCajaDTOBuilder totalEsperadoEfectivo(BigDecimal totalEsperadoEfectivo) { this.totalEsperadoEfectivo = totalEsperadoEfectivo; return this; }
        public ResumenCajaDTOBuilder totalEsperadoTarjeta(BigDecimal totalEsperadoTarjeta) { this.totalEsperadoTarjeta = totalEsperadoTarjeta; return this; }
        public ResumenCajaDTOBuilder totalPedidos(Integer totalPedidos) { this.totalPedidos = totalPedidos; return this; }
        public ResumenCajaDTOBuilder cantidadVouchers(Integer cantidadVouchers) { this.cantidadVouchers = cantidadVouchers; return this; }
        public ResumenCajaDTOBuilder cantidadTransferencias(Integer cantidadTransferencias) { this.cantidadTransferencias = cantidadTransferencias; return this; }

        public ResumenCajaDTO build() {
            return new ResumenCajaDTO(sesionId, estado, usuarioApertura, fechaApertura, montoInicial,
                    totalEfectivo, totalTarjetaDebito, totalTarjetaCredito, totalTransferenciaQr,
                    totalVentas, totalEsperadoEfectivo, totalEsperadoTarjeta,
                    totalPedidos, cantidadVouchers, cantidadTransferencias);
        }
    }
}
