package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.UnidadMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductoDTO {

    private Long id;
    private String codigoBarra;
    private String sku;
    private String nombre;
    private String descripcion;
    private TipoArticulo tipoArticulo;
    private BigDecimal precioVenta;
    private BigDecimal precioCosto;
    private TipoIva tipoIva;
    private BigDecimal stock;
    private BigDecimal stockMinimo;
    private UnidadMedida unidadMedida;
    private Boolean activo;

    // Específico ENVASE
    private Integer maxSabores;
    private Integer pesoGramosAprox;

    // Específico SABOR
    private CategoriaSabor categoriaSabor;
    private String categoria;
    private Boolean aptoCeliaco;
    private Boolean esVegano;
    private Boolean sinAzucar;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String imagenUrl;

    private List<ProveedorVinculadoDTO> proveedores = new ArrayList<>();
    private ProveedorVinculadoDTO proveedorPrincipal;
    private List<ComboComponenteDTO> componentes = new ArrayList<>();

    public ProductoDTO() {
    }

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
    public void setPrecioVenta(BigDecimal precioVenta) { this.precioVenta = precioVenta; }

    public BigDecimal getPrecioCosto() { return precioCosto; }
    public void setPrecioCosto(BigDecimal precioCosto) { this.precioCosto = precioCosto; }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; }

    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Integer getMaxSabores() { return maxSabores; }
    public void setMaxSabores(Integer maxSabores) { this.maxSabores = maxSabores; }

    public Integer getPesoGramosAprox() { return pesoGramosAprox; }
    public void setPesoGramosAprox(Integer pesoGramosAprox) { this.pesoGramosAprox = pesoGramosAprox; }

    public CategoriaSabor getCategoriaSabor() { return categoriaSabor; }
    public void setCategoriaSabor(CategoriaSabor categoriaSabor) { this.categoriaSabor = categoriaSabor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getAptoCeliaco() { return aptoCeliaco; }
    public void setAptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco; }

    public Boolean getEsVegano() { return esVegano; }
    public void setEsVegano(Boolean esVegano) { this.esVegano = esVegano; }

    public Boolean getSinAzucar() { return sinAzucar; }
    public void setSinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public List<ProveedorVinculadoDTO> getProveedores() { return proveedores; }
    public void setProveedores(List<ProveedorVinculadoDTO> proveedores) { this.proveedores = proveedores; }

    public ProveedorVinculadoDTO getProveedorPrincipal() { return proveedorPrincipal; }
    public void setProveedorPrincipal(ProveedorVinculadoDTO proveedorPrincipal) { this.proveedorPrincipal = proveedorPrincipal; }

    public List<ComboComponenteDTO> getComponentes() { return componentes; }
    public void setComponentes(List<ComboComponenteDTO> componentes) { this.componentes = componentes; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
}
