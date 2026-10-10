package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.CategoriaSabor;
import jakarta.persistence.*;

@Entity
@Table(name = "sabores")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Sabor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaSabor categoria;

    @Column(nullable = false)
    private Boolean aptoCeliaco = false;

    @Column(nullable = false)
    private Boolean esVegano = false;

    @Column(nullable = false)
    private Boolean sinAzucar = false;

    @Column(nullable = false)
    private Boolean disponible = true;

    @Column(precision = 6)
    private Double stockKilos;

    @Column(precision = 6)
    private Double stockMinimoKilos = 2.0;

    public Sabor() {
    }

    public Sabor(Long id, String nombre, String descripcion, CategoriaSabor categoria,
                 Boolean aptoCeliaco, Boolean esVegano, Boolean sinAzucar, Boolean disponible, Double stockKilos, Double stockMinimoKilos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.aptoCeliaco = aptoCeliaco != null ? aptoCeliaco : false;
        this.esVegano = esVegano != null ? esVegano : false;
        this.sinAzucar = sinAzucar != null ? sinAzucar : false;
        this.disponible = disponible != null ? disponible : true;
        this.stockKilos = stockKilos;
        this.stockMinimoKilos = stockMinimoKilos != null ? stockMinimoKilos : 2.0;
    }

    public static SaborBuilder builder() {
        return new SaborBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public CategoriaSabor getCategoria() { return categoria; }
    public void setCategoria(CategoriaSabor categoria) { this.categoria = categoria; }

    public Boolean getAptoCeliaco() { return aptoCeliaco; }
    public void setAptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco; }

    public Boolean getEsVegano() { return esVegano; }
    public void setEsVegano(Boolean esVegano) { this.esVegano = esVegano; }

    public Boolean getSinAzucar() { return sinAzucar; }
    public void setSinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Double getStockKilos() { return stockKilos; }
    public void setStockKilos(Double stockKilos) { this.stockKilos = stockKilos; }

    public Double getStockMinimoKilos() { return stockMinimoKilos; }
    public void setStockMinimoKilos(Double stockMinimoKilos) { this.stockMinimoKilos = stockMinimoKilos; }

    public static class SaborBuilder {
        private Long id;
        private String nombre;
        private String descripcion;
        private CategoriaSabor categoria;
        private Boolean aptoCeliaco = false;
        private Boolean esVegano = false;
        private Boolean sinAzucar = false;
        private Boolean disponible = true;
        private Double stockKilos;
        private Double stockMinimoKilos = 2.0;

        public SaborBuilder id(Long id) { this.id = id; return this; }
        public SaborBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public SaborBuilder descripcion(String descripcion) { this.descripcion = descripcion; return this; }
        public SaborBuilder categoria(CategoriaSabor categoria) { this.categoria = categoria; return this; }
        public SaborBuilder aptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco; return this; }
        public SaborBuilder esVegano(Boolean esVegano) { this.esVegano = esVegano; return this; }
        public SaborBuilder sinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar; return this; }
        public SaborBuilder disponible(Boolean disponible) { this.disponible = disponible; return this; }
        public SaborBuilder stockKilos(Double stockKilos) { this.stockKilos = stockKilos; return this; }
        public SaborBuilder stockMinimoKilos(Double stockMinimoKilos) { this.stockMinimoKilos = stockMinimoKilos; return this; }

        public Sabor build() {
            return new Sabor(id, nombre, descripcion, categoria, aptoCeliaco, esVegano, sinAzucar, disponible, stockKilos, stockMinimoKilos);
        }
    }
}
