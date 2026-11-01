package com.heladeria.api.dto;

import com.heladeria.api.entities.MovimientoCaja;
import com.heladeria.api.entities.enums.CategoriaMovimientoCaja;
import com.heladeria.api.entities.enums.TipoMovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoCajaDTO {

    private Long id;
    private Long sesionId;
    private TipoMovimientoCaja tipo;
    private CategoriaMovimientoCaja categoria;
    private String concepto;
    private String comprobante;
    private BigDecimal monto;
    private String usuario;
    private LocalDateTime fecha;

    public MovimientoCajaDTO() {
    }

    public MovimientoCajaDTO(Long id, Long sesionId, TipoMovimientoCaja tipo,
                             CategoriaMovimientoCaja categoria, String concepto,
                             String comprobante, BigDecimal monto, String usuario,
                             LocalDateTime fecha) {
        this.id = id;
        this.sesionId = sesionId;
        this.tipo = tipo;
        this.categoria = categoria;
        this.concepto = concepto;
        this.comprobante = comprobante;
        this.monto = monto;
        this.usuario = usuario;
        this.fecha = fecha;
    }

    public static MovimientoCajaDTO fromEntity(MovimientoCaja entity) {
        if (entity == null) {
            return null;
        }
        return new MovimientoCajaDTO(
                entity.getId(),
                entity.getSesionCaja() != null ? entity.getSesionCaja().getId() : null,
                entity.getTipo(),
                entity.getCategoria(),
                entity.getConcepto(),
                entity.getComprobante(),
                entity.getMonto(),
                entity.getUsuario(),
                entity.getFecha()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
