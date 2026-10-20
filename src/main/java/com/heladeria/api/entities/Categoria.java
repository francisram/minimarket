package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoArticulo;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_categoria_nombre", columnList = "nombre", unique = true),
    @Index(name = "idx_categoria_tipo", columnList = "tipo_articulo")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_articulo", length = 30)
    private TipoArticulo tipoArticulo;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public Categoria() {
    }

    public Categoria(Long id, String nombre, String descripcion, TipoArticulo tipoArticulo, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoArticulo = tipoArticulo;
        this.activo = activo != null ? activo : true;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) {
            this.activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    public static CategoriaBuilder builder() {
        return new CategoriaBuilder();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoArticulo getTipoArticulo() { return tipoArticulo; }
    public void setTipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public static class CategoriaBuilder {
        private Long id;
        private String nombre;
        private String descripcion;
        private TipoArticulo tipoArticulo;
        private Boolean activo = true;

        public CategoriaBuilder id(Long id) { this.id = id; return this; }
        public CategoriaBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public CategoriaBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public CategoriaBuilder tipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; return this; }
        public CategoriaBuilder activo(Boolean activo) { this.activo = activo; return this; }

        public Categoria build() {
            return new Categoria(id, nombre, descripcion, tipoArticulo, activo);
        }
    }
}
