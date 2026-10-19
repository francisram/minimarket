package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_productos_tipo", columnList = "tipo_articulo"),
    @Index(name = "idx_productos_barcode", columnList = "codigo_barra"),
    @Index(name = "idx_productos_activo", columnList = "activo")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barra", length = 50, unique = true)
    private String codigoBarra;

    @Column(name = "sku", length = 50)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_articulo", nullable = false, length = 30)
    private TipoArticulo tipoArticulo = TipoArticulo.VENTA_DIRECTA;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 0)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @Column(name = "precio_costo", precision = 12, scale = 0)
    private BigDecimal precioCosto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_iva", nullable = false, length = 20)
    private TipoIva tipoIva = TipoIva.IVA_10;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stock = BigDecimal.ZERO;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 2)
    private BigDecimal stockMinimo = new BigDecimal("5");

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @Column(nullable = false)
    private Boolean activo = true;

    // --- Atributos específicos para ENVASE ---
    @Column(name = "max_sabores")
    private Integer maxSabores;

    @Column(name = "peso_gramos_aprox")
    private Integer pesoGramosAprox;

    // --- Atributos específicos para SABOR ---
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_sabor", length = 30)
    private CategoriaSabor categoriaSabor;

    @Column(name = "apto_celiaco")
    private Boolean aptoCeliaco = true;

    @Column(name = "es_vegano")
    private Boolean esVegano = false;

    @Column(name = "sin_azucar")
    private Boolean sinAzucar = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // --- Asociación N:M con Proveedores ---
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ProductoProveedor> proveedores = new ArrayList<>();

    public Producto() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
        if (this.stock == null) this.stock = BigDecimal.ZERO;
        if (this.stockMinimo == null) this.stockMinimo = new BigDecimal("5");
        if (this.precioVenta == null) this.precioVenta = BigDecimal.ZERO;
        if (this.precioCosto == null) this.precioCosto = BigDecimal.ZERO;
        if (this.tipoIva == null) this.tipoIva = TipoIva.IVA_10;
        if (this.unidadMedida == null) this.unidadMedida = UnidadMedida.UNIDAD;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Helper methods para gestionar proveedores
    public void agregarProveedor(Proveedor proveedor, String codigoReferencia, BigDecimal costoCompra, Boolean esPrincipal) {
        // Si se marca como principal, desmarcar los anteriores
        if (Boolean.TRUE.equals(esPrincipal)) {
            for (ProductoProveedor pp : this.proveedores) {
                pp.setEsPrincipal(false);
            }
        }
        ProductoProveedor asociacion = new ProductoProveedor(this, proveedor, codigoReferencia, costoCompra, esPrincipal);
        this.proveedores.add(asociacion);
    }

    public void removerProveedor(Long proveedorId) {
        this.proveedores.removeIf(pp -> pp.getProveedor().getId().equals(proveedorId));
    }

    public ProductoProveedor getProveedorPrincipal() {
        if (this.proveedores == null || this.proveedores.isEmpty()) {
            return null;
        }
        return this.proveedores.stream()
                .filter(pp -> Boolean.TRUE.equals(pp.getEsPrincipal()))
                .findFirst()
                .orElse(this.proveedores.get(0));
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoArticulo getTipoArticulo() { return tipoArticulo; }
    public void setTipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; }

    public BigDecimal getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta != null ? precioVenta.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto != null ? precioCosto.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10; }

    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock != null ? stock : BigDecimal.ZERO; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo != null ? stockMinimo : new BigDecimal("5"); }

    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida != null ? unidadMedida : UnidadMedida.UNIDAD; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo != null ? activo : true; }

    public Integer getMaxSabores() { return maxSabores; }
    public void setMaxSabores(Integer maxSabores) { this.maxSabores = maxSabores; }

    public Integer getPesoGramosAprox() { return pesoGramosAprox; }
    public void setPesoGramosAprox(Integer pesoGramosAprox) { this.pesoGramosAprox = pesoGramosAprox; }

    public CategoriaSabor getCategoriaSabor() { return categoriaSabor; }
    public void setCategoriaSabor(CategoriaSabor categoriaSabor) { this.categoriaSabor = categoriaSabor; }

    public Boolean getAptoCeliaco() { return aptoCeliaco; }
    public void setAptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco != null ? aptoCeliaco : true; }

    public Boolean getEsVegano() { return esVegano; }
    public void setEsVegano(Boolean esVegano) { this.esVegano = esVegano != null ? esVegano : false; }

    public Boolean getSinAzucar() { return sinAzucar; }
    public void setSinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar != null ? sinAzucar : false; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<ProductoProveedor> getProveedores() { return proveedores; }
    public void setProveedores(List<ProductoProveedor> proveedores) { this.proveedores = proveedores; }
}
