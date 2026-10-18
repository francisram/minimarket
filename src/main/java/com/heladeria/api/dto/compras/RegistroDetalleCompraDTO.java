package com.heladeria.api.dto.compras;

import com.heladeria.api.entities.enums.TipoIva;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RegistroDetalleCompraDTO {

    @NotNull(message = "El id del producto es obligatorio.")
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria.")
    @Positive(message = "La cantidad debe ser mayor a cero.")
    private BigDecimal cantidad;

    @NotNull(message = "El costo unitario es obligatorio.")
    @Positive(message = "El costo unitario debe ser mayor a cero.")
    private BigDecimal costoUnitario;

    private TipoIva tipoIva = TipoIva.IVA_10;

    public RegistroDetalleCompraDTO() {
    }

    public RegistroDetalleCompraDTO(Long productoId, BigDecimal cantidad, BigDecimal costoUnitario, TipoIva tipoIva) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.tipoIva = tipoIva != null ? tipoIva : TipoIva.IVA_10;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public TipoIva getTipoIva() { return tipoIva; }
    public void setTipoIva(TipoIva tipoIva) { this.tipoIva = tipoIva; }
}
