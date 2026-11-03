package com.heladeria.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Estado de la licencia para avisar al operador. Solo informativo: no bloquea la operación. */
public record LicenciaEstadoDTO(
        /** SIN_LICENCIA, PERPETUA, VIGENTE, POR_VENCER o VENCIDA. */
        String estado,
        String planNombre,
        LocalDate fechaVencimiento,
        /** Negativo si ya venció; null si es perpetua o no hay licencia. */
        Long diasRestantes,
        String mensaje,
        boolean relojRetrocedido,
        String detalleReloj,
        LocalDateTime ultimaFechaVista
) { }
