package com.heladeria.api.dto;

import com.heladeria.api.entities.PuntoExpedicion;

import java.time.LocalDate;

/**
 * DTO de salida para información y estado operativo de un Punto de Expedición.
 */
public record PuntoExpedicionDTO(
        Long id,
        String nombre,
        String establecimiento,
        String puntoEmision,
        String prefijoLegal,
        String timbrado,
        LocalDate timbradoInicio,
        LocalDate timbradoVencimiento,
        Long numeroInicioFactura,
        Long numeroFinFactura,
        Long ultimoNumeroFactura,
        String proximaFactura,
        Boolean activo,
        Boolean enUso,
        String usuarioActual,
        Long sesionCajaIdActual
) {
    public static PuntoExpedicionDTO fromEntity(PuntoExpedicion p, boolean enUso, String usuarioActual, Long sesionCajaIdActual) {
        if (p == null) {
            return null;
        }
        return new PuntoExpedicionDTO(
                p.getId(),
                p.getNombre(),
                p.getEstablecimiento(),
                p.getPuntoEmision(),
                p.getPrefijoLegal(),
                p.getTimbrado(),
                p.getTimbradoInicio(),
                p.getTimbradoVencimiento(),
                p.getNumeroInicioFactura(),
                p.getNumeroFinFactura(),
                p.getUltimoNumeroFactura(),
                p.getProximaFacturaFormateada(),
                p.getActivo(),
                enUso,
                usuarioActual,
                sesionCajaIdActual
        );
    }

    public static PuntoExpedicionDTO fromEntity(PuntoExpedicion p) {
        return fromEntity(p, false, null, null);
    }
}
