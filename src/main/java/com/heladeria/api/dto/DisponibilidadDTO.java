package com.heladeria.api.dto;

import jakarta.validation.constraints.NotNull;

public class DisponibilidadDTO {
    @NotNull(message = "El valor de disponibilidad es obligatorio")
    private Boolean disponible;

    public DisponibilidadDTO() {
    }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}
