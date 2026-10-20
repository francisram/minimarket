package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "toppings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Topping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal precioExtra;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(name = "imagen_url", columnDefinition = "TEXT")
    private String imagenUrl;

    public Topping() {
    }

    public Topping(Long id, String nombre, BigDecimal precioExtra, Boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.precioExtra = precioExtra != null ? precioExtra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.disponible = disponible != null ? disponible : true;
    }

    public static ToppingBuilder builder() {
        return new ToppingBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioExtra() { return precioExtra; }
    public void setPrecioExtra(BigDecimal precioExtra) {
        this.precioExtra = precioExtra != null ? precioExtra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public static class ToppingBuilder {
        private Long id;
        private String nombre;
        private BigDecimal precioExtra;
        private Boolean disponible = true;
        private String imagenUrl;

        public ToppingBuilder id(Long id) { this.id = id; return this; }
        public ToppingBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ToppingBuilder precioExtra(BigDecimal precioExtra) { this.precioExtra = precioExtra; return this; }
        public ToppingBuilder disponible(Boolean disponible) { this.disponible = disponible; return this; }
        public ToppingBuilder imagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; return this; }

        public Topping build() {
            Topping t = new Topping(id, nombre, precioExtra, disponible);
            t.setImagenUrl(imagenUrl);
            return t;
        }
    }
}
