package com.heladeria.api.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "presentaciones")
public class Presentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer maxSabores;

    private Integer pesoGramosAprox;

    @Column(nullable = false)
    private Boolean activo = true;

    public Presentacion() {
    }

    public Presentacion(Long id, String nombre, BigDecimal precio, Integer maxSabores, Integer pesoGramosAprox, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.maxSabores = maxSabores;
        this.pesoGramosAprox = pesoGramosAprox;
        this.activo = activo != null ? activo : true;
    }

    public static PresentacionBuilder builder() {
        return new PresentacionBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getMaxSabores() { return maxSabores; }
    public void setMaxSabores(Integer maxSabores) { this.maxSabores = maxSabores; }

    public Integer getPesoGramosAprox() { return pesoGramosAprox; }
    public void setPesoGramosAprox(Integer pesoGramosAprox) { this.pesoGramosAprox = pesoGramosAprox; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public static class PresentacionBuilder {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        private Integer maxSabores;
        private Integer pesoGramosAprox;
        private Boolean activo = true;

        public PresentacionBuilder id(Long id) { this.id = id; return this; }
        public PresentacionBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public PresentacionBuilder precio(BigDecimal precio) { this.precio = precio; return this; }
        public PresentacionBuilder maxSabores(Integer maxSabores) { this.maxSabores = maxSabores; return this; }
        public PresentacionBuilder pesoGramosAprox(Integer pesoGramosAprox) { this.pesoGramosAprox = pesoGramosAprox; return this; }
        public PresentacionBuilder activo(Boolean activo) { this.activo = activo; return this; }

        public Presentacion build() {
            return new Presentacion(id, nombre, precio, maxSabores, pesoGramosAprox, activo);
        }
    }
}
