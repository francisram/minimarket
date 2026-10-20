package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoIva;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos_simples", indexes = {
    @Index(name = "idx_prod_simple_codigo_barra", columnList = "codigo_barra")
})
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

    @Column(name = "codigo_barra", length = 60)
    private String codigoBarra;

    @Column(name = "codigo", length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    public ProductoSimple() {
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo) {
        this(id, nombre, precio, categoria, stock, stockMinimo, activo, TipoIva.IVA_10, null, null);
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo, TipoIva tipoIva) {
        this(id, nombre, precio, categoria, stock, stockMinimo, activo, tipoIva, null, null);
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo, TipoIva tipoIva, String codigoBarra, String codigo, Proveedor proveedor) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio != null ? precio.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.categoria = categoria;
        this.stock = stock != null ? stock : 0;
        this.stockMinimo = stockMinimo != null ? stockMinimo : 5;
        this.activo = activo != null ? activo : true;
        this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10;
        this.codigoBarra = codigoBarra;
        this.codigo = codigo;
        this.proveedor = proveedor;
    }

    public ProductoSimple(Long id, String nombre, BigDecimal precio, String categoria, Integer stock, Integer stockMinimo, Boolean activo, TipoIva tipoIva, String codigoBarra, Proveedor proveedor) {
        this(id, nombre, precio, categoria, stock, stockMinimo, activo, tipoIva, codigoBarra, null, proveedor);
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

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public static class ProductoSimpleBuilder {
        private Long id;
        private String nombre;
        private BigDecimal precio;
        private String categoria;
        private Integer stock = 0;
        private Integer stockMinimo = 5;
        private Boolean activo = true;
        private TipoIva tipoIva = TipoIva.IVA_10;
        private String codigoBarra;
        private String codigo;
        private Proveedor proveedor;

        public ProductoSimpleBuilder id(Long id) { this.id = id; return this; }
        public ProductoSimpleBuilder nombre(String nombre) { this.nombre = nombre; return this; }
        public ProductoSimpleBuilder precio(BigDecimal precio) { this.precio = precio; return this; }
        public ProductoSimpleBuilder categoria(String categoria) { this.categoria = categoria; return this; }
        public ProductoSimpleBuilder stock(Integer stock) { this.stock = stock; return this; }
        public ProductoSimpleBuilder stockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; return this; }
        public ProductoSimpleBuilder activo(Boolean activo) { this.activo = activo; return this; }
        public ProductoSimpleBuilder tipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; return this; }
        public ProductoSimpleBuilder codigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; return this; }
        public ProductoSimpleBuilder codigo(String codigo) { this.codigo = codigo; return this; }
        public ProductoSimpleBuilder proveedor(Proveedor proveedor) { this.proveedor = proveedor; return this; }

        public ProductoSimple build() {
            return new ProductoSimple(id, nombre, precio, categoria, stock, stockMinimo, activo, tipoIva, codigoBarra, codigo, proveedor);
        }
    }
}
