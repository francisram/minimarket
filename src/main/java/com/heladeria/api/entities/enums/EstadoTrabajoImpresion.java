package com.heladeria.api.entities.enums;

public enum EstadoTrabajoImpresion {
    PENDIENTE,
    IMPRIMIENDO,
    IMPRESO,
    /** Agotó los reintentos (dead letter): requiere reintento manual. */
    MUERTO
}
