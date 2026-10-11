package com.heladeria.api.dto.reportes;

import java.math.BigDecimal;

public class ItemRankingDTO {
    private Long id;
    private String nombre;
    private String tipo;
    private long cantidad;
    private BigDecimal totalRecaudado;

    public ItemRankingDTO() {
    }

    public ItemRankingDTO(Long id, String nombre, String tipo, long cantidad, BigDecimal totalRecaudado) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.totalRecaudado = totalRecaudado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getCantidad() {
        return cantidad;
    }

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotalRecaudado() {
        return totalRecaudado;
    }

    public void setTotalRecaudado(BigDecimal totalRecaudado) {
        this.totalRecaudado = totalRecaudado;
    }
}
