package com.heladeria.api.dto.reportes;

import com.heladeria.api.entities.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SesionCajaReporteDTO {
    private Long id;
    private String cajeroApertura;
    private String usuarioCierre;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoEsperadoEfectivo;
    private BigDecimal montoRealEfectivo;
    private BigDecimal diferencia;
    private BigDecimal totalVentasGeneral;
    private int cantidadPedidos;
    private EstadoSesionCaja estado;

    public SesionCajaReporteDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCajeroApertura() {
        return cajeroApertura;
    }

    public void setCajeroApertura(String cajeroApertura) {
        this.cajeroApertura = cajeroApertura;
    }

    public String getUsuarioCierre() {
        return usuarioCierre;
    }

    public void setUsuarioCierre(String usuarioCierre) {
        this.usuarioCierre = usuarioCierre;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public BigDecimal getMontoEsperadoEfectivo() {
        return montoEsperadoEfectivo;
    }

    public void setMontoEsperadoEfectivo(BigDecimal montoEsperadoEfectivo) {
        this.montoEsperadoEfectivo = montoEsperadoEfectivo;
    }

    public BigDecimal getMontoRealEfectivo() {
        return montoRealEfectivo;
    }

    public void setMontoRealEfectivo(BigDecimal montoRealEfectivo) {
        this.montoRealEfectivo = montoRealEfectivo;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public BigDecimal getTotalVentasGeneral() {
        return totalVentasGeneral;
    }

    public void setTotalVentasGeneral(BigDecimal totalVentasGeneral) {
        this.totalVentasGeneral = totalVentasGeneral;
    }

    public int getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(int cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public EstadoSesionCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoSesionCaja estado) {
        this.estado = estado;
    }
}
