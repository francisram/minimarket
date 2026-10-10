package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteRequestDTO {

    @NotBlank(message = "El RUC o Cédula es obligatorio")
    @Size(max = 30, message = "El RUC no puede superar 30 caracteres")
    private String ruc;

    @NotBlank(message = "La Razón Social o Nombre es obligatorio")
    @Size(max = 120, message = "La Razón Social no puede superar 120 caracteres")
    private String razonSocial;

    @Size(max = 150, message = "La dirección no puede superar 150 caracteres")
    private String direccion;

    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    private String telefono;

    @Size(max = 80, message = "El email no puede superar 80 caracteres")
    private String email;

    private Boolean activo = true;

    public ClienteRequestDTO() {}

    public ClienteRequestDTO(String ruc, String razonSocial, String direccion, String telefono, String email, Boolean activo) {
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.activo = activo != null ? activo : true;
    }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
