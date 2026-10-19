package com.heladeria.api.dto.auditoria;

import com.heladeria.api.entities.AuditoriaLog;
import java.time.LocalDateTime;

public class AuditoriaLogDTO {

    private Long id;
    private LocalDateTime fecha;
    private String usuario;
    private String modulo;
    private String accion;
    private Long entidadId;
    private String entidadNombre;
    private String detalle;
    private String ipOrigen;

    public AuditoriaLogDTO() {
    }

    public AuditoriaLogDTO(AuditoriaLog log) {
        if (log != null) {
            this.id = log.getId();
            this.fecha = log.getFecha();
            this.usuario = log.getUsuario();
            this.modulo = log.getModulo();
            this.accion = log.getAccion();
            this.entidadId = log.getEntidadId();
            this.entidadNombre = log.getEntidadNombre();
            this.detalle = log.getDetalle();
            this.ipOrigen = log.getIpOrigen();
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
