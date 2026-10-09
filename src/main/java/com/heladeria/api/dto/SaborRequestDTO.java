package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.CategoriaSabor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SaborRequestDTO {

    @NotBlank(message = "El nombre del sabor es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La categoría del sabor es obligatoria")
    private CategoriaSabor categoria;

    private Boolean aptoCeliaco = false;
    private Boolean esVegano = false;
    private Boolean sinAzucar = false;
    private Boolean disponible = true;
    private Double stockKilos;

    public SaborRequestDTO() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public CategoriaSabor getCategoria() { return categoria; }
    public void setCategoria(CategoriaSabor categoria) { this.categoria = categoria; }

    public Boolean getAptoCeliaco() { return aptoCeliaco; }
    public void setAptoCeliaco(Boolean aptoCeliaco) { this.aptoCeliaco = aptoCeliaco; }

    public Boolean getEsVegano() { return esVegano; }
    public void setEsVegano(Boolean esVegano) { this.esVegano = esVegano; }

    public Boolean getSinAzucar() { return sinAzucar; }
    public void setSinAzucar(Boolean sinAzucar) { this.sinAzucar = sinAzucar; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public Double getStockKilos() { return stockKilos; }
    public void setStockKilos(Double stockKilos) { this.stockKilos = stockKilos; }
}
