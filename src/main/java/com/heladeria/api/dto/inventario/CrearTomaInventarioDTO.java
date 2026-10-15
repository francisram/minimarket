package com.heladeria.api.dto.inventario;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CrearTomaInventarioDTO {

    private String observaciones;

    @NotEmpty(message = "La toma de inventario debe incluir al menos un artículo contado")
    private List<ItemConteoDTO> detalles;

    public CrearTomaInventarioDTO() {
    }

    public CrearTomaInventarioDTO(String observaciones, List<ItemConteoDTO> detalles) {
        this.observaciones = observaciones;
        this.detalles = detalles;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<ItemConteoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<ItemConteoDTO> detalles) {
        this.detalles = detalles;
    }
}
