package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class UsuarioDTO {

    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    private String password;

    private Long rolId;
    private String rolNombre;
    private Boolean estado = true;
    private Boolean passwordNuncaExpira = false;
    private Boolean debeCambiarPassword = false;
    private LocalDate passwordExpiraEl;

    public UsuarioDTO() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Boolean getPasswordNuncaExpira() { return passwordNuncaExpira; }
    public void setPasswordNuncaExpira(Boolean passwordNuncaExpira) { this.passwordNuncaExpira = passwordNuncaExpira; }

    public Boolean getDebeCambiarPassword() { return debeCambiarPassword; }
    public void setDebeCambiarPassword(Boolean debeCambiarPassword) { this.debeCambiarPassword = debeCambiarPassword; }

    public LocalDate getPasswordExpiraEl() { return passwordExpiraEl; }
    public void setPasswordExpiraEl(LocalDate passwordExpiraEl) { this.passwordExpiraEl = passwordExpiraEl; }
}
