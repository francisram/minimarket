package com.heladeria.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EstadoCajaDTO {

    private boolean abierta;
    private Long sesionId;
    private String cajeroApertura;
    private LocalDateTime fechaApertura;
    private BigDecimal montoInicial;

    public EstadoCajaDTO() {
    }

    public EstadoCajaDTO(boolean abierta, Long sesionId, String cajeroApertura,
                         LocalDateTime fechaApertura, BigDecimal montoInicial) {
        this.abierta = abierta;
        this.sesionId = sesionId;
        this.cajeroApertura = cajeroApertura;
        this.fechaApertura = fechaApertura;
        this.montoInicial = montoInicial;
    }

    public boolean isAbierta() {
        return abierta;
    }

    public void setAbierta(boolean abierta) {
        this.abierta = abierta;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public String getCajeroApertura() {
        return cajeroApertura;
    }

    public void setCajeroApertura(String cajeroApertura) {
        this.cajeroApertura = cajeroApertura;
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
}
