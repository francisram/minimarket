package com.heladeria.api.dto.compras;

import com.heladeria.api.entities.Compra;
import com.heladeria.api.entities.enums.CondicionCompra;
import com.heladeria.api.entities.enums.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompraDTO {
    private Long id;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private String proveedorRuc;
    private String numeroFactura;
    private String timbrado;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private CondicionCompra condicionCompra;
    private EstadoCompra estado;
    private BigDecimal totalExenta;
    private BigDecimal totalIva5;
    private BigDecimal totalIva10;
    private BigDecimal total;
    private String observacion;
    private String usuario;
    private LocalDateTime fechaCreacion;
    private List<DetalleCompraDTO> detalles = new ArrayList<>();

    public CompraDTO() {
    }

    public CompraDTO(Compra compra) {
        if (compra != null) {
            this.id = compra.getId();
            if (compra.getProveedor() != null) {
                this.proveedorId = compra.getProveedor().getId();
                this.proveedorRazonSocial = compra.getProveedor().getRazonSocial();
                this.proveedorRuc = compra.getProveedor().getRuc();
            }
            this.numeroFactura = compra.getNumeroFactura();
            this.timbrado = compra.getTimbrado();
            this.fechaEmision = compra.getFechaEmision();
            this.fechaVencimiento = compra.getFechaVencimiento();
            this.condicionCompra = compra.getCondicionCompra();
            this.estado = compra.getEstado();
            this.totalExenta = compra.getTotalExenta();
            this.totalIva5 = compra.getTotalIva5();
            this.totalIva10 = compra.getTotalIva10();
            this.total = compra.getTotal();
            this.observacion = compra.getObservacion();
            this.usuario = compra.getUsuario();
            this.fechaCreacion = compra.getFechaCreacion();
            if (compra.getDetalles() != null) {
                this.detalles = compra.getDetalles().stream()
                        .map(DetalleCompraDTO::new)
                        .collect(Collectors.toList());
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

    public String getProveedorRazonSocial() { return proveedorRazonSocial; }
    public void setProveedorRazonSocial(String proveedorRazonSocial) { this.proveedorRazonSocial = proveedorRazonSocial; }

    public String getProveedorRuc() { return proveedorRuc; }
    public void setProveedorRuc(String proveedorRuc) { this.proveedorRuc = proveedorRuc; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public CondicionCompra getCondicionCompra() { return condicionCompra; }
    public void setCondicionCompra(CondicionCompra condicionCompra) { this.condicionCompra = condicionCompra; }

    public EstadoCompra getEstado() { return estado; }
    public void setEstado(EstadoCompra estado) { this.estado = estado; }

    public BigDecimal getTotalExenta() { return totalExenta; }
    public void setTotalExenta(BigDecimal totalExenta) { this.totalExenta = totalExenta; }

    public BigDecimal getTotalIva5() { return totalIva5; }
    public void setTotalIva5(BigDecimal totalIva5) { this.totalIva5 = totalIva5; }

    public BigDecimal getTotalIva10() { return totalIva10; }
    public void setTotalIva10(BigDecimal totalIva10) { this.totalIva10 = totalIva10; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public List<DetalleCompraDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleCompraDTO> detalles) { this.detalles = detalles; }
}
