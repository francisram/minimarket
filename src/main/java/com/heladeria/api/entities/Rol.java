package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Rol {

    @Id
    @Column(name = "id_rol")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rol_paginas",
        joinColumns = @JoinColumn(name = "id_rol"),
        inverseJoinColumns = @JoinColumn(name = "id_pagina")
    )
    private Set<Pagina> paginas = new HashSet<>();

    public Rol() {
    }

    public Rol(Long id, String nombreRol, Set<Pagina> paginas) {
        this.id = id;
        this.nombreRol = nombreRol;
        this.paginas = paginas != null ? paginas : new HashSet<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public Set<Pagina> getPaginas() { return paginas; }
    public void setPaginas(Set<Pagina> paginas) { this.paginas = paginas; }
}
