package com.heladeria.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Estado de salud de los respaldos: qué pasó con el último automático, su verificación y la copia externa. */
public record EstadoRespaldoDTO(
        BackupInfoDTO ultimoRespaldo,
        Long horasDesdeUltimoRespaldo,
        LocalDateTime ultimaEjecucionProgramada,
        /** OK, ERROR o null si aún no corrió. */
        String resultadoProgramado,
        String detalleProgramado,
        LocalDateTime fechaVerificacion,
        /** OK, ERROR o null. */
        String resultadoVerificacion,
        /** COMPLETA (restauración de prueba) o INTEGRIDAD (solo lectura del archivo). */
        String nivelVerificacion,
        String detalleVerificacion,
        boolean copiaExternaConfigurada,
        LocalDateTime fechaCopiaExterna,
        /** OK, ERROR, NO_CONFIGURADA o null. */
        String resultadoCopiaExterna,
        String detalleCopiaExterna,
        /** Problemas que requieren acción. */
        List<String> alertas,
        /** Avisos no críticos. */
        List<String> advertencias,
        boolean requiereAtencion
) { }
