package com.heladeria.api.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos_simples")
public class ProductoSimple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(length = 50)
    private String categoria;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    public ProductoSimple() {
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.stock = stock != null ? stock : 0;
        this.activo = activo != null ? activo : true;
    }

    public static ProductoSimpleBuilder builder() {
        return new ProductoSimpleBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public static class ProductoSimpleBuilder {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        private String categoria;
        private Integer stock = 0;
        private Boolean activo = true;

        public ProductoSimpleBuilder id(Long id) { this.id = id; return this; }
        public ProductoSimpleBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ProductoSimpleBuilder precio(BigDecimal precio) { this.precio = precio; return this; }
        public ProductoSimpleBuilder categoria(String categoria) { this.categoria = categoria; return this; }
        public ProductoSimpleBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductoSimpleBuilder activo(Boolean activo) { this.activo = activo; return this; }

        public ProductoSimple build() {
            return new ProductoSimple(id, nombre, precio, categoria, stock, activo);
        }
    }
}
