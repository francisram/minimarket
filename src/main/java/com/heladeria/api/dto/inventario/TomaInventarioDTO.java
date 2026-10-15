package com.heladeria.api.dto.inventario;

import com.heladeria.api.entities.enums.EstadoTomaInventario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TomaInventarioDTO {

    private Long id;
    private LocalDateTime fecha;
    private String usuarioAuditor;
    private String observaciones;
    private EstadoTomaInventario estado;
    private Integer totalArticulos;
    private Integer totalDiferencias;
    private BigDecimal impactoNeto;
    private List<DetalleTomaInventarioDTO> detalles;

    public TomaInventarioDTO() {
    }

    public TomaInventarioDTO(Long id, LocalDateTime fecha, String usuarioAuditor, String observaciones,
                             EstadoTomaInventario estado, Integer totalArticulos, Integer totalDiferencias,
                             BigDecimal impactoNeto, List<DetalleTomaInventarioDTO> detalles) {
        this.id = id;
        this.fecha = fecha;
        this.usuarioAuditor = usuarioAuditor;
        this.observaciones = observaciones;
        this.estado = estado;
        this.totalArticulos = totalArticulos;
        this.totalDiferencias = totalDiferencias;
        this.impactoNeto = impactoNeto;
        this.detalles = detalles;
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

    public String getUsuarioAuditor() {
        return usuarioAuditor;
    }

    public void setUsuarioAuditor(String usuarioAuditor) {
        this.usuarioAuditor = usuarioAuditor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoTomaInventario getEstado() {
        return estado;
    }

    public void setEstado(EstadoTomaInventario estado) {
        this.estado = estado;
    }

    public Integer getTotalArticulos() {
        return totalArticulos;
    }

    public void setTotalArticulos(Integer totalArticulos) {
        this.totalArticulos = totalArticulos;
    }

    public Integer getTotalDiferencias() {
        return totalDiferencias;
    }

    public void setTotalDiferencias(Integer totalDiferencias) {
        this.totalDiferencias = totalDiferencias;
    }

    public BigDecimal getImpactoNeto() {
        return impactoNeto;
    }

    public void setImpactoNeto(BigDecimal impactoNeto) {
        this.impactoNeto = impactoNeto;
    }

    public List<DetalleTomaInventarioDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleTomaInventarioDTO> detalles) {
        this.detalles = detalles;
    }
}
