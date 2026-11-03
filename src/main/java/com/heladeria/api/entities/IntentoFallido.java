package com.heladeria.api.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Contador de intentos fallidos de una clave del limitador (p. ej. "login:cajero1"). */
@Entity
@Table(name = "intentos_fallidos")
public class IntentoFallido {

    @Id
    @Column(length = 150)
    private String clave;

    @Column(nullable = false)
    private int fallos;

    @Column(name = "primer_fallo")
    private LocalDateTime primerFallo;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    public IntentoFallido() { }

    public IntentoFallido(String clave) {
        this.clave = clave;
    }

    public String getClave() { return clave; }
    public int getFallos() { return fallos; }
    public void setFallos(int fallos) { this.fallos = fallos; }

    public Instant getPrimerFallo() { return primerFallo == null ? null : primerFallo.toInstant(ZoneOffset.UTC); }
    public void setPrimerFallo(Instant i) { this.primerFallo = i == null ? null : LocalDateTime.ofInstant(i, ZoneOffset.UTC); }

    public Instant getBloqueadoHasta() { return bloqueadoHasta == null ? null : bloqueadoHasta.toInstant(ZoneOffset.UTC); }
    public void setBloqueadoHasta(Instant i) { this.bloqueadoHasta = i == null ? null : LocalDateTime.ofInstant(i, ZoneOffset.UTC); }
}
