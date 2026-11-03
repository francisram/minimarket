package com.heladeria.api.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Sesión de autenticación: un inicio de sesión que se renueva con un token de refresco rotativo. */
@Entity
@Table(name = "sesiones_auth")
public class SesionAuth {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "refresh_hash", nullable = false, length = 64)
    private String refreshHash;

    @Column(name = "refresh_hash_anterior", length = 64)
    private String refreshHashAnterior;

    @Column(name = "rotado_en")
    private LocalDateTime rotadoEn;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "revocada_en")
    private LocalDateTime revocadaEn;

    @Column(length = 200)
    private String dispositivo;

    public SesionAuth() { }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getRefreshHash() { return refreshHash; }
    public void setRefreshHash(String refreshHash) { this.refreshHash = refreshHash; }
    public String getRefreshHashAnterior() { return refreshHashAnterior; }
    public void setRefreshHashAnterior(String refreshHashAnterior) { this.refreshHashAnterior = refreshHashAnterior; }
    public LocalDateTime getRotadoEn() { return rotadoEn; }
    public void setRotadoEn(LocalDateTime rotadoEn) { this.rotadoEn = rotadoEn; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
    public LocalDateTime getExpiraEn() { return expiraEn; }
    public void setExpiraEn(LocalDateTime expiraEn) { this.expiraEn = expiraEn; }
    public LocalDateTime getRevocadaEn() { return revocadaEn; }
    public void setRevocadaEn(LocalDateTime revocadaEn) { this.revocadaEn = revocadaEn; }
    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }

    public boolean estaActiva(LocalDateTime ahora) {
        return revocadaEn == null && expiraEn.isAfter(ahora);
    }
}
