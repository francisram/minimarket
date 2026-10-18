package com.heladeria.api.dto.compras;

import com.heladeria.api.entities.enums.CondicionCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class RegistroCompraRequestDTO {

    @NotNull(message = "El proveedor es obligatorio.")
    private Long proveedorId;

    @NotBlank(message = "El número de factura es obligatorio.")
    private String numeroFactura;

    private String timbrado;

    @NotNull(message = "La fecha de emisión es obligatoria.")
    private LocalDate fechaEmision;

    private LocalDate fechaVencimiento;

    private CondicionCompra condicionCompra = CondicionCompra.CONTADO;

    private String observacion;

    @NotEmpty(message = "Debe incluir al menos un detalle de compra.")
    @Valid
    private List<RegistroDetalleCompraDTO> detalles;

    public RegistroCompraRequestDTO() {
    }

    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }

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

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public List<RegistroDetalleCompraDTO> getDetalles() { return detalles; }
    public void setDetalles(List<RegistroDetalleCompraDTO> detalles) { this.detalles = detalles; }
}
