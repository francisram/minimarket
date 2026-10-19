package com.heladeria.api.dto.auditoria;

public class ResumenAuditoriaDTO {

    private long totalEventos;
    private long totalUsuarios;
    private long totalModificaciones;
    private long totalBajasOAnulaciones;

    public ResumenAuditoriaDTO() {
    }

    public ResumenAuditoriaDTO(long totalEventos, long totalUsuarios, long totalModificaciones, long totalBajasOAnulaciones) {
        this.totalEventos = totalEventos;
        this.totalUsuarios = totalUsuarios;
        this.totalModificaciones = totalModificaciones;
        this.totalBajasOAnulaciones = totalBajasOAnulaciones;
    }

    public long getTotalEventos() {
        return totalEventos;
    }

    public void setTotalEventos(long totalEventos) {
        this.totalEventos = totalEventos;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getTotalModificaciones() {
        return totalModificaciones;
    }

    public void setTotalModificaciones(long totalModificaciones) {
        this.totalModificaciones = totalModificaciones;
    }

    public long getTotalBajasOAnulaciones() {
        return totalBajasOAnulaciones;
    }

    public void setTotalBajasOAnulaciones(long totalBajasOAnulaciones) {
        this.totalBajasOAnulaciones = totalBajasOAnulaciones;
    }
}
