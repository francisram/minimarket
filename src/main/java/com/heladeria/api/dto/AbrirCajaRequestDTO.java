package com.heladeria.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AbrirCajaRequestDTO {

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0", inclusive = true, message = "El monto inicial no puede ser negativo")
    private BigDecimal montoInicial;

    private String observaciones;

    public AbrirCajaRequestDTO() {
    }

    public AbrirCajaRequestDTO(BigDecimal montoInicial, String observaciones) {
        this.montoInicial = montoInicial;
        this.observaciones = observaciones;
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
}
