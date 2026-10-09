package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ImpresoraRequestDTO {

    @NotBlank(message = "nombre es requerido")
    private String nombre;

    private String tipoConexion;

    @NotBlank(message = "ip es requerida")
    private String ip;

    private Integer puerto;

    private String cola;

    public ImpresoraRequestDTO() {
    }

    public ImpresoraRequestDTO(String nombre, String tipoConexion, String ip, Integer puerto, String cola) {
        this.nombre = nombre;
        this.tipoConexion = tipoConexion;
        this.ip = ip;
        this.puerto = puerto;
        this.cola = cola;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoConexion() {
        return tipoConexion;
    }

    public void setTipoConexion(String tipoConexion) {
        this.tipoConexion = tipoConexion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPuerto() {
        return puerto;
    }

    public void setPuerto(Integer puerto) {
        this.puerto = puerto;
    }

    public String getCola() {
        return cola;
    }

    public void setCola(String cola) {
        this.cola = cola;
    }
}
