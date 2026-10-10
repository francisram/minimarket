package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "impresoras")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Impresora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_conexion", nullable = false, length = 10)
    private TipoConexion tipoConexion;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false)
    private Integer puerto;

    /** Solo CUPS: nombre de la cola remota. Null para RED. */
    @Column(length = 100)
    private String cola;

    public Impresora() {
    }

    public Impresora(Long id, String nombre, TipoConexion tipoConexion, String ip, Integer puerto, String cola) {
        this.id = id;
        this.nombre = nombre;
        this.tipoConexion = tipoConexion;
        this.ip = ip;
        this.puerto = puerto;
        this.cola = cola;
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
