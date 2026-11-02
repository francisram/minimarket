package com.heladeria.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AbrirCajaRequestDTO {

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El monto inicial no puede ser negativo")
    private BigDecimal montoInicial;

    private String observaciones;

    private Long puntoExpedicionId;

    public AbrirCajaRequestDTO() {
    }

    public AbrirCajaRequestDTO(BigDecimal montoInicial, String observaciones) {
        this.montoInicial = montoInicial;
        this.observaciones = observaciones;
    }

    public AbrirCajaRequestDTO(BigDecimal montoInicial, String observaciones, Long puntoExpedicionId) {
        this.montoInicial = montoInicial;
        this.observaciones = observaciones;
        this.puntoExpedicionId = puntoExpedicionId;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getPuntoExpedicionId() {
        return puntoExpedicionId;
    }

    public void setPuntoExpedicionId(Long puntoExpedicionId) {
        this.puntoExpedicionId = puntoExpedicionId;
    }
}
