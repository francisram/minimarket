package com.heladeria.api.exceptions;

public class PasswordExpiradaException extends RuntimeException {

    private final String motivo;

    public PasswordExpiradaException(String motivo, String message) {
        super(message);
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }
}
