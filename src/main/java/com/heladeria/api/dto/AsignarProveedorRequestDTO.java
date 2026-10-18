package com.heladeria.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AsignarProveedorRequestDTO {

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    private String codigoReferencia;

    private BigDecimal costoCompra;

    private Boolean esPrincipal = false;

    public AsignarProveedorRequestDTO() {
    }

    public AsignarProveedorRequestDTO(Long proveedorId, String codigoReferencia, BigDecimal costoCompra, Boolean esPrincipal) {
        this.proveedorId = proveedorId;
        this.codigoReferencia = codigoReferencia;
        this.costoCompra = costoCompra;
        this.esPrincipal = esPrincipal != null ? esPrincipal : false;
    }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getCodigoReferencia() { return codigoReferencia; }
    public void setCodigoReferencia(String codigoReferencia) { this.codigoReferencia = codigoReferencia; }

    public BigDecimal getCostoCompra() { return costoCompra; }
    public void setCostoCompra(BigDecimal costoCompra) { this.costoCompra = costoCompra; }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal != null ? esPrincipal : false; }
}
