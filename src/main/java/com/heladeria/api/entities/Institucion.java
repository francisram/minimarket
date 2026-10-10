package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Recurso singleton: una única fila (id=1) para toda la instalación —
 * nombre, logo (base64) y datos fiscales del negocio emisor para facturación e impresión.
 */
@Entity
@Table(name = "institucion")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    @Column(length = 30)
    private String ruc;

    @Column(length = 30)
    private String timbrado;

    @Column
    private LocalDate timbradoVencimiento;

    @Column(length = 10)
    private String establecimiento = "001";

    @Column(length = 10)
    private String puntoEmision = "001";

    @Column
    private Long ultimoNumeroFactura = 0L;

    @Column(length = 200)
    private String direccion;

    @Column(length = 50)
    private String telefono;

    @Column(length = 100)
    private String ciudad = "Asunción";

    public Institucion() {
    }

    public Institucion(Long id, String nombre, String logoBase64) {
        this.id = id;
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
        this.establecimiento = "001";
        this.puntoEmision = "001";
        this.ultimoNumeroFactura = 0L;
        this.ciudad = "Asunción";
    }

    public Institucion(Long id, String nombre, String logoBase64, String ruc, String timbrado,
                       LocalDate timbradoVencimiento, String establecimiento, String puntoEmision,
                       Long ultimoNumeroFactura, String direccion, String telefono, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
        this.ruc = ruc;
        this.timbrado = timbrado;
        this.timbradoVencimiento = timbradoVencimiento;
        this.establecimiento = establecimiento != null ? establecimiento : "001";
        this.puntoEmision = puntoEmision != null ? puntoEmision : "001";
        this.ultimoNumeroFactura = ultimoNumeroFactura != null ? ultimoNumeroFactura : 0L;
        this.direccion = direccion;
        this.telefono = telefono;
        this.ciudad = ciudad != null ? ciudad : "Asunción";
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.establecimiento == null) this.establecimiento = "001";
        if (this.puntoEmision == null) this.puntoEmision = "001";
        if (this.ultimoNumeroFactura == null) this.ultimoNumeroFactura = 0L;
        if (this.ciudad == null) this.ciudad = "Asunción";
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

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(String timbrado) {
        this.timbrado = timbrado;
    }

    public LocalDate getTimbradoVencimiento() {
        return timbradoVencimiento;
    }

    public void setTimbradoVencimiento(LocalDate timbradoVencimiento) {
        this.timbradoVencimiento = timbradoVencimiento;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoEmision() {
        return puntoEmision;
    }

    public void setPuntoEmision(String puntoEmision) {
        this.puntoEmision = puntoEmision;
    }

    public Long getUltimoNumeroFactura() {
        return ultimoNumeroFactura;
    }

    public void setUltimoNumeroFactura(Long ultimoNumeroFactura) {
        this.ultimoNumeroFactura = ultimoNumeroFactura;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
