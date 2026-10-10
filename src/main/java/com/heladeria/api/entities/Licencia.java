package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "licencia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Licencia {

    @Id
    private Long id = 1L;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanLicencia plan;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    public Licencia() {
    }

    public Licencia(Long id, PlanLicencia plan, LocalDate fechaVencimiento) {
        this.id = id != null ? id : 1L;
        this.plan = plan;
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PlanLicencia getPlan() { return plan; }
    public void setPlan(PlanLicencia plan) { this.plan = plan; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
