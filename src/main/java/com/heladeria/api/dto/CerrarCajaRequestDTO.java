package com.heladeria.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CerrarCajaRequestDTO {

    private Long sesionId;

    @NotNull(message = "El monto real en efectivo es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El monto real en efectivo no puede ser negativo")
    private BigDecimal montoRealEfectivo;

    private String observaciones;

    public CerrarCajaRequestDTO() {
    }

    public CerrarCajaRequestDTO(BigDecimal montoRealEfectivo, String observaciones) {
        this.montoRealEfectivo = montoRealEfectivo;
        this.observaciones = observaciones;
    }

    public CerrarCajaRequestDTO(Long sesionId, BigDecimal montoRealEfectivo, String observaciones) {
        this.sesionId = sesionId;
        this.montoRealEfectivo = montoRealEfectivo;
        this.observaciones = observaciones;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public BigDecimal getMontoRealEfectivo() {
        return montoRealEfectivo;
    }

    public void setMontoRealEfectivo(BigDecimal montoRealEfectivo) {
        this.montoRealEfectivo = montoRealEfectivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
