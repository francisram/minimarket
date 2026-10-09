package com.heladeria.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class LicenciaDTO {

    private Long id = 1L;

    @NotNull(message = "El ID del plan es obligatorio")
    private Long planId;

    private String planNombre;
    private Set<String> paginas = new HashSet<>();
    private LocalDate fechaVencimiento;

    public LicenciaDTO() {
    }

    public LicenciaDTO(Long id, Long planId, String planNombre, Set<String> paginas, LocalDate fechaVencimiento) {
        this.id = id != null ? id : 1L;
        this.planId = planId;
        this.planNombre = planNombre;
        this.paginas = paginas != null ? paginas : new HashSet<>();
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPlanNombre() { return planNombre; }
    public void setPlanNombre(String planNombre) { this.planNombre = planNombre; }

    public Set<String> getPaginas() { return paginas; }
    public void setPaginas(Set<String> paginas) { this.paginas = paginas != null ? paginas : new HashSet<>(); }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
