package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.UnidadMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductoRequestDTO {

    @Size(max = 50, message = "El código de barra no puede exceder 50 caracteres")
    private String codigoBarra;

    @Size(max = 50, message = "El SKU no puede exceder 50 caracteres")
    private String sku;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El tipo de artículo es obligatorio")
    private TipoArticulo tipoArticulo;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0", message = "El precio de venta no puede ser negativo")
    private BigDecimal precioVenta;

    @DecimalMin(value = "0", message = "El precio de costo no puede ser negativo")
    private BigDecimal precioCosto;

    private TipoIva tipoIva = TipoIva.IVA_10;

    private BigDecimal stock = BigDecimal.ZERO;

    private BigDecimal stockMinimo = new BigDecimal("5");

    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    private Boolean activo = true;

    // Atributos ENVASE
    private Integer maxSabores;
    private Integer pesoGramosAprox;

    // Atributos SABOR
    private CategoriaSabor categoriaSabor;
    private String categoria;
    private Boolean aptoCeliaco = true;
    private Boolean esVegano = false;
    private Boolean sinAzucar = false;

    // Lista de proveedores a vincular
    private List<AsignarProveedorRequestDTO> proveedores = new ArrayList<>();
    private List<ComboComponenteDTO> componentes = new ArrayList<>();

    public ProductoRequestDTO() {
    }

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

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Boolean getAptoCeliaco() { return aptoCeliaco; }
    public void setAptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco != null ? aptoCeliaco : true; }

    public Boolean getEsVegano() { return esVegano; }
    public void setEsVegano(Boolean esVegano) { this.esVegano = esVegano != null ? esVegano : false; }

    public Boolean getSinAzucar() { return sinAzucar; }
    public void setSinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar != null ? sinAzucar : false; }

    public List<AsignarProveedorRequestDTO> getProveedores() { return proveedores; }
    public void setProveedores(List<AsignarProveedorRequestDTO> proveedores) { this.proveedores = proveedores; }

    public List<ComboComponenteDTO> getComponentes() { return componentes; }
    public void setComponentes(List<ComboComponenteDTO> componentes) { this.componentes = componentes; }
}
