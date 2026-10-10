package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "paginas")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pagina {

    @Id
    @Column(name = "id_pagina")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String clave;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String url;

    @Column(name = "id_padre")
    private Long idPadre;

    @Column(length = 50)
    private String icono;

    @Column(nullable = false)
    private Integer orden = 0;

    public Pagina() {
    }

    public Pagina(Long id, String clave, String nombre, String url, Long idPadre, String icono, Integer orden) {
        this.id = id;
        this.clave = clave;
        this.nombre = nombre;
        this.url = url;
        this.idPadre = idPadre;
        this.icono = icono;
        this.orden = orden != null ? orden : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Long getIdPadre() { return idPadre; }
    public void setIdPadre(Long idPadre) { this.idPadre = idPadre; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
