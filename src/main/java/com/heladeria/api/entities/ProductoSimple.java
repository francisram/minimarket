package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoIva;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos_simples")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductoSimple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal precio;

    @Column(length = 50)
    private String categoria;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(columnDefinition = "integer default 5")
    private Integer stockMinimo = 5;

    @Column(nullable = false)
    private Boolean activo = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva", nullable = false, length = 20, columnDefinition = "varchar(20) default 'IVA_10'")
    private TipoIva tipoIva = TipoIva.IVA_10;

    public ProductoSimple() {
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo) {
        this(id, nombre, precio, categoria, stock, stockMinimo, activo, TipoIva.IVA_10);
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo, TipoIva tipoIva) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio != null ? precio.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.categoria = categoria;
        this.stock = stock != null ? stock : 0;
        this.stockMinimo = stockMinimo != null ? stockMinimo : 5;
        this.activo = activo != null ? activo : true;
        this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10;
    }

    public static ProductoSimpleBuilder builder() {
        return new ProductoSimpleBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) {
        this.precio = precio != null ? precio.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) {
        this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10;
    }

    public static class ProductoSimpleBuilder {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        private String categoria;
        private Integer stock = 0;
        private Integer stockMinimo = 5;
        private Boolean activo = true;
        private TipoIva tipoIva = TipoIva.IVA_10;

        public ProductoSimpleBuilder id(Long id) { this.id = id; return this; }
        public ProductoSimpleBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ProductoSimpleBuilder precio(BigDecimal precio) { this.precio = precio; return this; }
        public ProductoSimpleBuilder categoria(String categoria) { this.categoria = categoria; return this; }
        public ProductoSimpleBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductoSimpleBuilder stockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; return this; }
        public ProductoSimpleBuilder activo(Boolean activo) { this.activo = activo; return this; }
        public ProductoSimpleBuilder tipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; return this; }

        public ProductoSimple build() {
            return new ProductoSimple(id, nombre, precio, categoria, stock, stockMinimo, activo, tipoIva);
        }
    }
}
