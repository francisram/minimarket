package com.heladeria.api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Recurso singleton: una única fila (id=1) para toda la instalación —
 * nombre y logo (base64) del negocio, usados para membretar tickets/recibos impresos.
 */
@Entity
@Table(name = "institucion")
public class Institucion {

    @Id
    private Long id;

    @Column(nullable = false)
    private String nombre;

    /**
     * Imagen del logo codificada en base64, tal cual la manda el cliente
     * (sin prefijo data URI). Nula si todavía no se cargó un logo.
     */
    @Column(name = "logo_base64", columnDefinition = "TEXT")
    private String logoBase64;

    public Institucion() {
    }

    public Institucion(Long id, String nombre, String logoBase64) {
        this.id = id;
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
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

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }
}
