package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.EstadoPedido;
import jakarta.validation.constraints.NotNull;

public class ActualizarEstadoDTO {
    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoPedido nuevoEstado;

    public ActualizarEstadoDTO() {
    }

    public EstadoPedido getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(EstadoPedido nuevoEstado) { this.nuevoEstado = nuevoEstado; }
}
