package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Usuario {

    @Id
    @Column(name = "id_usuario")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    @JsonIgnore
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "password_nunca_expira", nullable = false)
    private Boolean passwordNuncaExpira = false;

    @Column(name = "debe_cambiar_password", nullable = false)
    private Boolean debeCambiarPassword = false;

    @Column(name = "password_expira_el")
    private LocalDate passwordExpiraEl;

    public Usuario() {
    }

    public Usuario(Long id, String username, String passwordHash, Rol rol, Boolean estado,
                   Boolean passwordNuncaExpira, Boolean debeCambiarPassword, LocalDate passwordExpiraEl) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.estado = estado != null ? estado : true;
        this.passwordNuncaExpira = passwordNuncaExpira != null ? passwordNuncaExpira : false;
        this.debeCambiarPassword = debeCambiarPassword != null ? debeCambiarPassword : false;
        this.passwordExpiraEl = passwordExpiraEl;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public Boolean getPasswordNuncaExpira() { return passwordNuncaExpira; }
    public void setPasswordNuncaExpira(Boolean passwordNuncaExpira) { this.passwordNuncaExpira = passwordNuncaExpira; }

    public Boolean getDebeCambiarPassword() { return debeCambiarPassword; }
    public void setDebeCambiarPassword(Boolean debeCambiarPassword) { this.debeCambiarPassword = debeCambiarPassword; }

    public LocalDate getPasswordExpiraEl() { return passwordExpiraEl; }
    public void setPasswordExpiraEl(LocalDate passwordExpiraEl) { this.passwordExpiraEl = passwordExpiraEl; }
}
