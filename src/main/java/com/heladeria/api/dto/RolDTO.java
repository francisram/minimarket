package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

public class RolDTO {

    private Long id;

    @NotBlank(message = "El nombre del rol es obligatorio")
    private String nombre;

    private Set<String> paginas = new HashSet<>();

    public RolDTO() {
    }

    public RolDTO(Long id, String nombre, Set<String> paginas) {
        this.id = id;
        this.nombre = nombre;
        this.paginas = paginas != null ? paginas : new HashSet<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<String> getPaginas() { return paginas; }
    public void setPaginas(Set<String> paginas) { this.paginas = paginas != null ? paginas : new HashSet<>(); }
}
