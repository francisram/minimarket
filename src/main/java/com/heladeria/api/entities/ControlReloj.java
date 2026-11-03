package com.heladeria.api.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Fila única con la fecha más alta que el sistema vio, para detectar si el reloj del servidor se atrasó. */
@Entity
@Table(name = "control_reloj")
public class ControlReloj {

    @Id
    private Long id = 1L;

    @Column(name = "ultima_fecha_vista")
    private LocalDateTime ultimaFechaVista;

    @Column(name = "retroceso_detectado", nullable = false)
    private boolean retrocesoDetectado;

    /** Hora (del reloj atrasado) en que se detectó el retroceso. */
    @Column(name = "fecha_retroceso")
    private LocalDateTime fechaRetroceso;

    /** Fecha más alta ya vista que el reloj actual no alcanza. */
    @Column(name = "referencia_retroceso")
    private LocalDateTime referenciaRetroceso;

    @Column(name = "desfase_minutos")
    private Long desfaseMinutos;

    public Long getId() { return id; }
    public LocalDateTime getUltimaFechaVista() { return ultimaFechaVista; }
    public void setUltimaFechaVista(LocalDateTime v) { this.ultimaFechaVista = v; }
    public boolean isRetrocesoDetectado() { return retrocesoDetectado; }
    public void setRetrocesoDetectado(boolean v) { this.retrocesoDetectado = v; }
    public LocalDateTime getFechaRetroceso() { return fechaRetroceso; }
    public void setFechaRetroceso(LocalDateTime v) { this.fechaRetroceso = v; }
    public LocalDateTime getReferenciaRetroceso() { return referenciaRetroceso; }
    public void setReferenciaRetroceso(LocalDateTime v) { this.referenciaRetroceso = v; }
    public Long getDesfaseMinutos() { return desfaseMinutos; }
    public void setDesfaseMinutos(Long v) { this.desfaseMinutos = v; }
}
