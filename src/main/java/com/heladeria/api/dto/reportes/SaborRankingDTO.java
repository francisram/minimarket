package com.heladeria.api.dto.reportes;

public class SaborRankingDTO {
    private Long id;
    private String nombre;
    private String categoria;
    private long cantidadElecciones;
    private double kilosEstimados;

    public SaborRankingDTO() {
    }

    public SaborRankingDTO(Long id, String nombre, String categoria, long cantidadElecciones, double kilosEstimados) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidadElecciones = cantidadElecciones;
        this.kilosEstimados = kilosEstimados;
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public long getCantidadElecciones() {
        return cantidadElecciones;
    }

    public void setCantidadElecciones(long cantidadElecciones) {
        this.cantidadElecciones = cantidadElecciones;
    }

    public double getKilosEstimados() {
        return kilosEstimados;
    }

    public void setKilosEstimados(double kilosEstimados) {
        this.kilosEstimados = kilosEstimados;
    }
}
