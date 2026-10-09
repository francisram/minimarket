package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CambiarPasswordDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña actual es obligatoria")
    private String passwordActual;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    private String passwordNueva;

    public CambiarPasswordDTO() {
    }

    public CambiarPasswordDTO(String username, String passwordActual, String passwordNueva) {
        this.username = username;
        this.passwordActual = passwordActual;
        this.passwordNueva = passwordNueva;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordActual() { return passwordActual; }
    public void setPasswordActual(String passwordActual) { this.passwordActual = passwordActual; }

    public String getPasswordNueva() { return passwordNueva; }
    public void setPasswordNueva(String passwordNueva) { this.passwordNueva = passwordNueva; }
}
