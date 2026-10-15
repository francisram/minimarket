package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.EstadoTomaInventario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tomas_inventario", indexes = {
    @Index(name = "idx_toma_fecha", columnList = "fecha"),
    @Index(name = "idx_toma_estado", columnList = "estado")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TomaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "usuario_auditor", nullable = false, length = 100)
    private String usuarioAuditor;

    @Column(length = 255)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoTomaInventario estado = EstadoTomaInventario.APLICADA;

    @Column(name = "total_articulos", nullable = false)
    private Integer totalArticulos = 0;

    @Column(name = "total_diferencias", nullable = false)
    private Integer totalDiferencias = 0;

    @Column(name = "impacto_neto", precision = 14, scale = 0)
    private BigDecimal impactoNeto = BigDecimal.ZERO;

    @OneToMany(mappedBy = "tomaInventario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleTomaInventario> detalles = new ArrayList<>();

    public TomaInventario() {
    }

    public TomaInventario(Long id, LocalDateTime fecha, String usuarioAuditor, String observaciones,
                          EstadoTomaInventario estado, Integer totalArticulos, Integer totalDiferencias,
                          BigDecimal impactoNeto, List<DetalleTomaInventario> detalles) {
        this.id = id;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.usuarioAuditor = usuarioAuditor;
        this.observaciones = observaciones;
        this.estado = estado != null ? estado : EstadoTomaInventario.APLICADA;
        this.totalArticulos = totalArticulos != null ? totalArticulos : 0;
        this.totalDiferencias = totalDiferencias != null ? totalDiferencias : 0;
        this.impactoNeto = impactoNeto != null ? impactoNeto : BigDecimal.ZERO;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }

    public void agregarDetalle(DetalleTomaInventario detalle) {
        detalles.add(detalle);
        detalle.setTomaInventario(this);
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

    public List<DetalleTomaInventario> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleTomaInventario> detalles) {
        this.detalles = detalles;
    }
}
