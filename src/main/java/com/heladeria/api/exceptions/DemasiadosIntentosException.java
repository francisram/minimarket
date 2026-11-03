package com.heladeria.api.exceptions;

/** Se superó el límite de intentos fallidos de una operación sensible (se mapea a HTTP 429). */
public class DemasiadosIntentosException extends RuntimeException {
    public DemasiadosIntentosException(String mensaje) {
        super(mensaje);
    }
}
