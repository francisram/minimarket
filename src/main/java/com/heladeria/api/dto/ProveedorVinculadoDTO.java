package com.heladeria.api.dto;

import java.math.BigDecimal;

public class ProveedorVinculadoDTO {
    private Long proveedorId;
    private String ruc;
    private String razonSocial;
    private String contacto;
    private String telefono;
    private String codigoReferencia;
    private BigDecimal costoCompra;
    private Boolean esPrincipal;

    public ProveedorVinculadoDTO() {
    }

    public ProveedorVinculadoDTO(Long proveedorId, String ruc, String razonSocial, String contacto,
                                 String telefono, String codigoReferencia, BigDecimal costoCompra, Boolean esPrincipal) {
        this.proveedorId = proveedorId;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.contacto = contacto;
        this.telefono = telefono;
        this.codigoReferencia = codigoReferencia;
        this.costoCompra = costoCompra != null ? costoCompra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.esPrincipal = esPrincipal != null ? esPrincipal : false;
    }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCodigoReferencia() { return codigoReferencia; }
    public void setCodigoReferencia(String codigoReferencia) { this.codigoReferencia = codigoReferencia; }

    public BigDecimal getCostoCompra() { return costoCompra; }
    public void setCostoCompra(BigDecimal costoCompra) {
        this.costoCompra = costoCompra != null ? costoCompra.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public Boolean getEsPrincipal() { return esPrincipal; }
    public void setEsPrincipal(Boolean esPrincipal) { this.esPrincipal = esPrincipal; }
}
