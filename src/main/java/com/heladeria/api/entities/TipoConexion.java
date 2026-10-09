package com.heladeria.api.entities;

/**
 * RED: impresora con IP propia, se le habla por socket TCP crudo (JetDirect,
 * puerto típico 9100).
 * CUPS: impresora USB compartida en red por un servidor CUPS (cola IPP, puerto típico 631).
 */
public enum TipoConexion {
    RED,
    CUPS
}
