package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ToppingRequestDTO {

    @NotBlank(message = "El nombre del topping o agregado es obligatorio")
    private String nombre;

    @NotNull(message = "El precio extra es obligatorio")
    @PositiveOrZero(message = "El precio extra debe ser cero o positivo")
    private BigDecimal precioExtra;

    private Boolean disponible = true;

    public ToppingRequestDTO() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioExtra() { return precioExtra; }
    public void setPrecioExtra(BigDecimal precioExtra) { this.precioExtra = precioExtra; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}
