package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "planes_licencia")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PlanLicencia {

    @Id
    @Column(name = "id_plan")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "plan_paginas",
        joinColumns = @JoinColumn(name = "id_plan"),
        inverseJoinColumns = @JoinColumn(name = "id_pagina")
    )
    private Set<Pagina> paginas = new HashSet<>();

    public PlanLicencia() {
    }

    public PlanLicencia(Long id, String nombre, Set<Pagina> paginas) {
        this.id = id;
        this.nombre = nombre;
        this.paginas = paginas != null ? paginas : new HashSet<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Pagina> getPaginas() { return paginas; }
    public void setPaginas(Set<Pagina> paginas) { this.paginas = paginas; }
}
