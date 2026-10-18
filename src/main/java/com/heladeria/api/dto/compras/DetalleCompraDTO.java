package com.heladeria.api.dto.compras;

import com.heladeria.api.entities.DetalleCompra;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.UnidadMedida;

import java.math.BigDecimal;

public class DetalleCompraDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private String codigoBarra;
    private TipoArticulo tipoArticulo;
    private UnidadMedida unidadMedida;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
    private TipoIva tipoIva;
    private BigDecimal montoIva;

    public DetalleCompraDTO() {
    }

    public DetalleCompraDTO(DetalleCompra detalle) {
        if (detalle != null) {
            this.id = detalle.getId();
            if (detalle.getProducto() != null) {
                this.productoId = detalle.getProducto().getId();
                this.productoNombre = detalle.getProducto().getNombre();
                this.codigoBarra = detalle.getProducto().getCodigoBarra();
                this.tipoArticulo = detalle.getProducto().getTipoArticulo();
                this.unidadMedida = detalle.getProducto().getUnidadMedida();
            }
            this.cantidad = detalle.getCantidad();
            this.costoUnitario = detalle.getCostoUnitario();
            this.subtotal = detalle.getSubtotal();
            this.tipoIva = detalle.getTipoIva();
            this.montoIva = detalle.getMontoIva();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public TipoArticulo getTipoArticulo() { return tipoArticulo; }
    public void setTipoArticulo(TipoArticulo tipoArticulo) { this.tipoArticulo = tipoArticulo; }

    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(UnidadMedida unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; }

    public BigDecimal getMontoIva() { return montoIva; }
    public void setMontoIva(BigDecimal montoIva) { this.montoIva = montoIva; }
}
