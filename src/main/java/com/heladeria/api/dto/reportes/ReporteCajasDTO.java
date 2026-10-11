package com.heladeria.api.dto.reportes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReporteCajasDTO {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFin;

    private long totalSesiones = 0;
    private BigDecimal totalVentasGeneral = BigDecimal.ZERO;
    private BigDecimal totalSobrante = BigDecimal.ZERO;
    private BigDecimal totalFaltante = BigDecimal.ZERO;

    private List<SesionCajaReporteDTO> sesiones = new ArrayList<>();

    public ReporteCajasDTO() {
    }

    public LocalDateTime getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(LocalDateTime periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    public LocalDateTime getPeriodoFin() {
        return periodoFin;
    }

    public void setPeriodoFin(LocalDateTime periodoFin) {
        this.periodoFin = periodoFin;
    }

    public long getTotalSesiones() {
        return totalSesiones;
    }

    public void setTotalSesiones(long totalSesiones) {
        this.totalSesiones = totalSesiones;
    }

    public BigDecimal getTotalVentasGeneral() {
        return totalVentasGeneral;
    }

    public void setTotalVentasGeneral(BigDecimal totalVentasGeneral) {
        this.totalVentasGeneral = totalVentasGeneral;
    }

    public BigDecimal getTotalSobrante() {
        return totalSobrante;
    }

    public void setTotalSobrante(BigDecimal totalSobrante) {
        this.totalSobrante = totalSobrante;
    }

    public BigDecimal getTotalFaltante() {
        return totalFaltante;
    }

    public void setTotalFaltante(BigDecimal totalFaltante) {
        this.totalFaltante = totalFaltante;
    }

    public List<SesionCajaReporteDTO> getSesiones() {
        return sesiones;
    }

    public void setSesiones(List<SesionCajaReporteDTO> sesiones) {
        this.sesiones = sesiones;
    }
}
