package com.heladeria.api.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/** Timbrado y numeración de las notas de crédito de un punto de expedición (distintos de los de las facturas). */
@Entity
@Table(name = "series_nota_credito")
public class SerieNotaCredito {

    @Id
    @Column(name = "punto_expedicion_id")
    private Long puntoExpedicionId;

    @Column(length = 30)
    private String timbrado;

    @Column(name = "timbrado_inicio")
    private LocalDate timbradoInicio;

    @Column(name = "timbrado_vencimiento")
    private LocalDate timbradoVencimiento;

    @Column(name = "numero_inicio")
    private Long numeroInicio;

    @Column(name = "numero_fin")
    private Long numeroFin;

    @Column(name = "ultimo_numero", nullable = false)
    private Long ultimoNumero = 0L;

    public SerieNotaCredito() { }

    public SerieNotaCredito(Long puntoExpedicionId) { this.puntoExpedicionId = puntoExpedicionId; }

    public Long getPuntoExpedicionId() { return puntoExpedicionId; }
    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }
    public LocalDate getTimbradoInicio() { return timbradoInicio; }
    public void setTimbradoInicio(LocalDate v) { this.timbradoInicio = v; }
    public LocalDate getTimbradoVencimiento() { return timbradoVencimiento; }
    public void setTimbradoVencimiento(LocalDate v) { this.timbradoVencimiento = v; }
    public Long getNumeroInicio() { return numeroInicio; }
    public void setNumeroInicio(Long v) { this.numeroInicio = v; }
    public Long getNumeroFin() { return numeroFin; }
    public void setNumeroFin(Long v) { this.numeroFin = v; }
    public Long getUltimoNumero() { return ultimoNumero; }
    public void setUltimoNumero(Long v) { this.ultimoNumero = v; }
}
