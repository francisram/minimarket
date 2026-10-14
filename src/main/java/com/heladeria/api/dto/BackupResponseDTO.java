package com.heladeria.api.dto;

public class BackupResponseDTO {

    private boolean exitoso;
    private String mensaje;
    private BackupInfoDTO backup;

    public BackupResponseDTO() {
    }

    public BackupResponseDTO(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public BackupResponseDTO(boolean exitoso, String mensaje, BackupInfoDTO backup) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.backup = backup;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public BackupInfoDTO getBackup() {
        return backup;
    }

    public void setBackup(BackupInfoDTO backup) {
        this.backup = backup;
    }
}
