package com.heladeria.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CerrarCajaRequestDTO {

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
