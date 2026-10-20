package com.heladeria.api.dto;

import com.heladeria.api.entities.Categoria;
import com.heladeria.api.entities.enums.TipoArticulo;
import java.time.LocalDateTime;

public class CategoriaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private TipoArticulo tipoArticulo;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public CategoriaDTO() {
    }

    public CategoriaDTO(Categoria c) {
        if (c != null) {
            this.id = c.getId();
            this.nombre = c.getNombre();
            this.descripcion = c.getDescripcion();
            this.tipoArticulo = c.getTipoArticulo();
            this.activo = c.getActivo();
            this.fechaCreacion = c.getFechaCreacion();
            this.fechaActualizacion = c.getFechaActualizacion();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoArticulo getTipoArticulo() { return tipoArticulo; }
    public void setTipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
