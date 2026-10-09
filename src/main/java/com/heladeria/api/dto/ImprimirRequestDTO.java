package com.heladeria.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ImprimirRequestDTO {

    @NotBlank(message = "texto es requerido")
    private String texto;

    private boolean incluirLogo = false;

    public ImprimirRequestDTO() {
    }

    public ImprimirRequestDTO(String texto, boolean incluirLogo) {
        this.texto = texto;
        this.incluirLogo = incluirLogo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isIncluirLogo() {
        return incluirLogo;
    }

    public void setIncluirLogo(boolean incluirLogo) {
        this.incluirLogo = incluirLogo;
    }
}
