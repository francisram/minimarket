package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.TipoArticulo;
import java.math.BigDecimal;

public class ComboComponenteDTO {

    private Long id;
    private Long componenteId;
    private String componenteNombre;
    private String componenteSku;
    private TipoArticulo componenteTipo;
    private BigDecimal costoUnitario;
    private BigDecimal precioUnitario;
    private BigDecimal cantidad = BigDecimal.ONE;
    private String notas;

    public ComboComponenteDTO() {
    }

    public ComboComponenteDTO(Long id, Long componenteId, String componenteNombre, String componenteSku,
                              TipoArticulo componenteTipo, BigDecimal costoUnitario, BigDecimal precioUnitario,
                              BigDecimal cantidad, String notas) {
        this.id = id;
        this.componenteId = componenteId;
        this.componenteNombre = componenteNombre;
        this.componenteSku = componenteSku;
        this.componenteTipo = componenteTipo;
        this.costoUnitario = costoUnitario;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.notas = notas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getComponenteId() { return componenteId; }
    public void setComponenteId(Long componenteId) { this.componenteId = componenteId; }

    public String getComponenteNombre() { return componenteNombre; }
    public void setComponenteNombre(String componenteNombre) { this.componenteNombre = componenteNombre; }

    public String getComponenteSku() { return componenteSku; }
    public void setComponenteSku(String componenteSku) { this.componenteSku = componenteSku; }

    public TipoArticulo getComponenteTipo() { return componenteTipo; }
    public void setComponenteTipo(TipoArticulo componenteTipo) { this.componenteTipo = componenteTipo; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
