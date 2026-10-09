package com.heladeria.api.dto;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.TipoConexion;

public class ImpresoraDTO {

    private Long id;
    private String nombre;
    private TipoConexion tipoConexion;
    private String ip;
    private Integer puerto;
    private String cola;

    public ImpresoraDTO() {
    }

    public ImpresoraDTO(Long id, String nombre, TipoConexion tipoConexion, String ip, Integer puerto, String cola) {
        this.id = id;
        this.nombre = nombre;
        this.tipoConexion = tipoConexion;
        this.ip = ip;
        this.puerto = puerto;
        this.cola = cola;
    }

    public static ImpresoraDTO from(Impresora impresora) {
        if (impresora == null) {
            return null;
        }
        return new ImpresoraDTO(
                impresora.getId(),
                impresora.getNombre(),
                impresora.getTipoConexion(),
                impresora.getIp(),
                impresora.getPuerto(),
                impresora.getCola()
        );
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

    public TipoConexion getTipoConexion() {
        return tipoConexion;
    }

    public void setTipoConexion(TipoConexion tipoConexion) {
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
