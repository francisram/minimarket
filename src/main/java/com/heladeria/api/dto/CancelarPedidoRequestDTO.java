package com.heladeria.api.dto;

public class CancelarPedidoRequestDTO {

    private String adminPassword;
    private String adminUsername;
    private String motivo;

    public CancelarPedidoRequestDTO() {
    }

    public CancelarPedidoRequestDTO(String adminPassword, String adminUsername, String motivo) {
        this.adminPassword = adminPassword;
        this.adminUsername = adminUsername;
        this.motivo = motivo;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
