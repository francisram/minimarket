package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.CategoriaMovimientoCaja;
import com.heladeria.api.entities.enums.TipoMovimientoCaja;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RegistrarMovimientoCajaRequestDTO {

    private Long sesionId;

    @NotNull(message = "El tipo de movimiento (EGRESO o INGRESO) es obligatorio.")
    private TipoMovimientoCaja tipo;

    @NotNull(message = "La categoría del movimiento es obligatoria.")
    private CategoriaMovimientoCaja categoria;

    @NotBlank(message = "El concepto o motivo del movimiento es obligatorio.")
    @Size(max = 200, message = "El concepto no puede superar los 200 caracteres.")
    private String concepto;

    @Size(max = 50, message = "El comprobante no puede superar los 50 caracteres.")
    private String comprobante;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "1", message = "El monto debe ser mayor a 0 Guaraníes.")
    private BigDecimal monto;

    public RegistrarMovimientoCajaRequestDTO() {
    }

    public RegistrarMovimientoCajaRequestDTO(Long sesionId, TipoMovimientoCaja tipo,
                                            CategoriaMovimientoCaja categoria, String concepto,
                                            String comprobante, BigDecimal monto) {
        this.sesionId = sesionId;
        this.tipo = tipo;
        this.categoria = categoria;
        this.concepto = concepto;
        this.comprobante = comprobante;
        this.monto = monto;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoCaja tipo) {
        this.tipo = tipo;
    }

    public CategoriaMovimientoCaja getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMovimientoCaja categoria) {
        this.categoria = categoria;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
