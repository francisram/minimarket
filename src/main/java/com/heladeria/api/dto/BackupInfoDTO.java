package com.heladeria.api.dto;

import java.time.LocalDateTime;

public class BackupInfoDTO {

    private String nombreArchivo;
    private long tamanoBytes;
    private String tamanoLegible;
    private LocalDateTime fechaCreacion;

    public BackupInfoDTO() {
    }

    public BackupInfoDTO(String nombreArchivo, long tamanoBytes, String tamanoLegible, LocalDateTime fechaCreacion) {
        this.nombreArchivo = nombreArchivo;
        this.tamanoBytes = tamanoBytes;
        this.tamanoLegible = tamanoLegible;
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getTamanoLegible() {
        return tamanoLegible;
    }

    public void setTamanoLegible(String tamanoLegible) {
        this.tamanoLegible = tamanoLegible;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
