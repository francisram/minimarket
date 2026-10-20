package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.TipoArticulo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    private TipoArticulo tipoArticulo;

    private Boolean activo = true;

    public CategoriaRequestDTO() {
    }

    public CategoriaRequestDTO(String nombre, String descripcion, TipoArticulo tipoArticulo, Boolean activo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoArticulo = tipoArticulo;
        this.activo = activo != null ? activo : true;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoArticulo getTipoArticulo() { return tipoArticulo; }
    public void setTipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
