package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_logs", indexes = {
    @Index(name = "idx_auditoria_fecha", columnList = "fecha"),
    @Index(name = "idx_auditoria_usuario", columnList = "usuario"),
    @Index(name = "idx_auditoria_modulo", columnList = "modulo"),
    @Index(name = "idx_auditoria_accion", columnList = "accion")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuditoriaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "usuario", nullable = false, length = 60)
    private String usuario;

    @Column(name = "modulo", nullable = false, length = 40)
    private String modulo;

    @Column(name = "accion", nullable = false, length = 30)
    private String accion;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(name = "entidad_nombre", length = 150)
    private String entidadNombre;

    @Column(name = "detalle", nullable = false, columnDefinition = "TEXT")
    private String detalle;

    @Column(name = "ip_origen", length = 50)
    private String ipOrigen;

    public AuditoriaLog() {
    }

    public AuditoriaLog(String usuario, String modulo, String accion, Long entidadId, String entidadNombre, String detalle, String ipOrigen) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.modulo = modulo;
        this.accion = accion;
        this.entidadId = entidadId;
        this.entidadNombre = entidadNombre;
        this.detalle = detalle;
        this.ipOrigen = ipOrigen;
    }

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getEntidadNombre() {
        return entidadNombre;
    }

    public void setEntidadNombre(String entidadNombre) {
        this.entidadNombre = entidadNombre;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }
}
