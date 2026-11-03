package com.heladeria.api.entities;

import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Cola persistente de impresión (outbox): se crea junto con la venta y se procesa después del commit. */
@Entity
@Table(name = "trabajos_impresion", indexes = {
        @Index(name = "idx_trabajo_impresion_estado", columnList = "estado, proximo_intento")
})
public class TrabajoImpresion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    /** Null = primera impresora configurada. */
    @Column(name = "impresora_id")
    private Long impresoraId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTrabajoImpresion estado;

    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "max_intentos", nullable = false)
    private Integer maxIntentos = 6;

    @Column(name = "proximo_intento", nullable = false)
    private LocalDateTime proximoIntento;

    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;

    @Column(length = 50)
    private String usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public Long getImpresoraId() { return impresoraId; }
    public void setImpresoraId(Long impresoraId) { this.impresoraId = impresoraId; }
    public EstadoTrabajoImpresion getEstado() { return estado; }
    public void setEstado(EstadoTrabajoImpresion estado) { this.estado = estado; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public Integer getMaxIntentos() { return maxIntentos; }
    public void setMaxIntentos(Integer maxIntentos) { this.maxIntentos = maxIntentos; }
    public LocalDateTime getProximoIntento() { return proximoIntento; }
    public void setProximoIntento(LocalDateTime proximoIntento) { this.proximoIntento = proximoIntento; }
    public String getUltimoError() { return ultimoError; }
    public void setUltimoError(String ultimoError) { this.ultimoError = ultimoError; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
