package com.sanmartin.minimarketbase.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "codigo_barra", nullable = false, unique = true, length = 50)
    private String codigoBarra;

    @Column(name = "codigo_interno", length = 50)
    private String codigoInterno;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "id_categoria", nullable = false)
    private Integer idCategoria;

    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "id_impuesto")
    private Integer idImpuesto;

    @Column(name = "precio_costo", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "precio_mayorista", precision = 12, scale = 2)
    private BigDecimal precioMayorista;

    @Column(name = "descuento_maximo", precision = 5, scale = 2)
    private BigDecimal descuentoMaximo;

    @Column(name = "stock_minimo", columnDefinition = "integer default 0")
    private Integer stockMinimo;

    @Column(name = "unidad_medida", length = 20, nullable = false)
    private String unidadMedida;

    @Column(name = "peso_neto", precision = 10, scale = 3)
    private BigDecimal pesoNeto;

    @Column(precision = 10, scale = 3)
    private BigDecimal volumen;

    @Column(length = 1)
    private String estado = "A";

    @Column(name = "es_perecedero")
    private Boolean esPerecedero = false;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "proveedor_principal")
    private Integer proveedorPrincipal;

    @Column(name = "ubicacion_gondola", length = 50)
    private String ubicacionGondola;

    // Getters y Setters
    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
    }

    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(String codigoInterno) {
        this.codigoInterno = codigoInterno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public Integer getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Integer idMarca) {
        this.idMarca = idMarca;
    }

    public Integer getIdImpuesto() {
        return idImpuesto;
    }

    public void setIdImpuesto(Integer idImpuesto) {
        this.idImpuesto = idImpuesto;
    }

    public BigDecimal getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(BigDecimal precioCosto) {
        this.precioCosto = precioCosto;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public BigDecimal getPrecioMayorista() {
        return precioMayorista;
    }

    public void setPrecioMayorista(BigDecimal precioMayorista) {
        this.precioMayorista = precioMayorista;
    }

    public BigDecimal getDescuentoMaximo() {
        return descuentoMaximo;
    }

    public void setDescuentoMaximo(BigDecimal descuentoMaximo) {
        this.descuentoMaximo = descuentoMaximo;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getPesoNeto() {
        return pesoNeto;
    }

    public void setPesoNeto(BigDecimal pesoNeto) {
        this.pesoNeto = pesoNeto;
    }

    public BigDecimal getVolumen() {
        return volumen;
    }

    public void setVolumen(BigDecimal volumen) {
        this.volumen = volumen;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getEsPerecedero() {
        return esPerecedero;
    }

    public void setEsPerecedero(Boolean esPerecedero) {
        this.esPerecedero = esPerecedero;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Integer getProveedorPrincipal() {
        return proveedorPrincipal;
    }

    public void setProveedorPrincipal(Integer proveedorPrincipal) {
        this.proveedorPrincipal = proveedorPrincipal;
    }

    public String getUbicacionGondola() {
        return ubicacionGondola;
    }

    public void setUbicacionGondola(String ubicacionGondola) {
        this.ubicacionGondola = ubicacionGondola;
    }
}