package com.heladeria.api.dto.reportes;

import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.TipoComprobante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ItemLibroIvaDTO {
    private Long pedidoId;
    private LocalDateTime fecha;
    private TipoComprobante tipoComprobante;
    private CondicionVenta condicionVenta;
    private String numeroComprobante;
    private String clienteRuc;
    private String clienteNombre;
    private BigDecimal exentas = BigDecimal.ZERO;
    private BigDecimal gravada5 = BigDecimal.ZERO;
    private BigDecimal iva5 = BigDecimal.ZERO;
    private BigDecimal gravada10 = BigDecimal.ZERO;
    private BigDecimal iva10 = BigDecimal.ZERO;
    private BigDecimal totalIva = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;

    public ItemLibroIvaDTO() {
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(TipoComprobante tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public CondicionVenta getCondicionVenta() {
        return condicionVenta;
    }

    public void setCondicionVenta(CondicionVenta condicionVenta) {
        this.condicionVenta = condicionVenta;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public String getClienteRuc() {
        return clienteRuc;
    }

    public void setClienteRuc(String clienteRuc) {
        this.clienteRuc = clienteRuc;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public BigDecimal getExentas() {
        return exentas;
    }

    public void setExentas(BigDecimal exentas) {
        this.exentas = exentas;
    }

    public BigDecimal getGravada5() {
        return gravada5;
    }

    public void setGravada5(BigDecimal gravada5) {
        this.gravada5 = gravada5;
    }

    public BigDecimal getIva5() {
        return iva5;
    }

    public void setIva5(BigDecimal iva5) {
        this.iva5 = iva5;
    }

    public BigDecimal getGravada10() {
        return gravada10;
    }

    public void setGravada10(BigDecimal gravada10) {
        this.gravada10 = gravada10;
    }

    public BigDecimal getIva10() {
        return iva10;
    }

    public void setIva10(BigDecimal iva10) {
        this.iva10 = iva10;
    }

    public BigDecimal getTotalIva() {
        return totalIva;
    }

    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
