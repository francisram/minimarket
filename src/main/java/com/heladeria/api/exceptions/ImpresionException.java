package com.heladeria.api.exceptions;

/**
 * Excepción de dominio para fallas de impresión (conectividad TCP/RED o subproceso CUPS).
 * Se mapea a HTTP 503 con mensaje en texto plano.
 */
public class ImpresionException extends RuntimeException {
    public ImpresionException(String message) {
        super(message);
    }
}
