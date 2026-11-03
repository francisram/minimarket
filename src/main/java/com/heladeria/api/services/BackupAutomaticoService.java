package com.heladeria.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.BackupInfoDTO;
import com.heladeria.api.dto.EstadoRespaldoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Respaldo automático: crea el dump, lo verifica con una restauración de prueba y lo copia fuera del servidor.
 * El resultado queda guardado en disco (estado_respaldo.json) para sobrevivir a reinicios y alimentar el aviso en pantalla.
 * La retención (borrado de respaldos viejos) es manual a cargo del operador.
 */
@Service
public class BackupAutomaticoService {

    private static final Logger log = LoggerFactory.getLogger(BackupAutomaticoService.class);
    private static final String ARCHIVO_ESTADO = "estado_respaldo.json";

    /** Estado persistido del último ciclo automático. */
    public static class Estado {
        public LocalDateTime ultimaEjecucion;
        public String resultadoProgramado;
        public String detalleProgramado;
        public LocalDateTime fechaVerificacion;
        public String resultadoVerificacion;
        public String nivelVerificacion;
        public String detalleVerificacion;
        public LocalDateTime fechaCopiaExterna;
        public String resultadoCopiaExterna;
        public String detalleCopiaExterna;
    }

    private final BackupService backupService;
    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;
    private final ReentrantLock lock = new ReentrantLock();

    @Value("${app.backup.max-horas:26}")
    private long maxHoras;

    @Value("${app.backup.verificar-restauracion:true}")
    private boolean verificarRestauracion;

    public BackupAutomaticoService(BackupService backupService, AuditoriaService auditoriaService, ObjectMapper objectMapper) {
        this.backupService = backupService;
        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
    }

    /** Ejecuta un ciclo completo. Devuelve false si ya había otro en curso. Nunca lanza excepciones. */
    public boolean ejecutarCiclo() {
        if (!lock.tryLock()) {
            log.warn("Ya hay un respaldo automático en curso; se omite esta ejecución.");
            return false;
        }
        try {
            Estado e = new Estado();
            e.ultimaEjecucion = LocalDateTime.now();
            String nombre = null;

            try {
                BackupInfoDTO info = backupService.crearRespaldo();
                nombre = info.getNombreArchivo();
                e.resultadoProgramado = "OK";
                e.detalleProgramado = nombre + " (" + info.getTamanoLegible() + ")";
            } catch (Exception ex) {
                e.resultadoProgramado = "ERROR";
                e.detalleProgramado = ex.getMessage();
                log.error("Falló el respaldo automático: {}", ex.getMessage());
            }

            if (nombre != null && verificarRestauracion) {
                e.fechaVerificacion = LocalDateTime.now();
                try {
                    BackupService.ResultadoVerificacion v = backupService.verificarRespaldo(nombre);
                    e.resultadoVerificacion = "OK";
                    e.nivelVerificacion = v.nivel();
                    e.detalleVerificacion = v.detalle();
                } catch (Exception ex) {
                    e.resultadoVerificacion = "ERROR";
                    e.detalleVerificacion = ex.getMessage();
                    log.error("El respaldo '{}' NO pasó la verificación: {}", nombre, ex.getMessage());
                }
            }

            if (nombre != null) {
                if (backupService.copiaExternaConfigurada()) {
                    e.fechaCopiaExterna = LocalDateTime.now();
                    try {
                        backupService.copiarACopiaExterna(nombre);
                        e.resultadoCopiaExterna = "OK";
                        e.detalleCopiaExterna = "Copiado a la ubicación externa.";
                    } catch (Exception ex) {
                        e.resultadoCopiaExterna = "ERROR";
                        e.detalleCopiaExterna = ex.getMessage();
                        log.error("Falló la copia externa de '{}': {}", nombre, ex.getMessage());
                    }
                } else {
                    e.resultadoCopiaExterna = "NO_CONFIGURADA";
                }
            }

            guardarEstado(e);
            boolean ok = "OK".equals(e.resultadoProgramado) && !"ERROR".equals(e.resultadoVerificacion) && !"ERROR".equals(e.resultadoCopiaExterna);
            auditoriaService.registrarLog("sistema", "BACKUP", ok ? "CREAR" : "ERROR", null, nombre != null ? nombre : "respaldo automático",
                    "Respaldo automático: " + e.resultadoProgramado
                            + (e.resultadoVerificacion != null ? " | verificación: " + e.resultadoVerificacion : "")
                            + (e.resultadoCopiaExterna != null ? " | copia externa: " + e.resultadoCopiaExterna : "")
                            + (ok ? "" : " | " + e.detalleProgramado + " " + (e.detalleVerificacion != null ? e.detalleVerificacion : "")
                            + " " + (e.detalleCopiaExterna != null ? e.detalleCopiaExterna : "")),
                    "localhost");
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Si el último respaldo es más viejo que el máximo permitido (p. ej. el servidor estuvo apagado a la hora programada). */
    public boolean respaldoVencido() {
        List<BackupInfoDTO> lista = backupService.listarRespaldos();
        if (lista.isEmpty()) {
            return true;
        }
        return Duration.between(lista.get(0).getFechaCreacion(), LocalDateTime.now()).toHours() >= maxHoras;
    }

    public EstadoRespaldoDTO obtenerEstado() {
        Estado e = leerEstado();
        List<BackupInfoDTO> lista = backupService.listarRespaldos();
        BackupInfoDTO ultimo = lista.isEmpty() ? null : lista.get(0);
        Long horas = ultimo != null ? Duration.between(ultimo.getFechaCreacion(), LocalDateTime.now()).toHours() : null;

        List<String> alertas = new ArrayList<>();
        List<String> advertencias = new ArrayList<>();
        if (ultimo == null) {
            alertas.add("No existe ningún respaldo de la base de datos.");
        } else if (horas >= maxHoras) {
            alertas.add("El último respaldo tiene " + horas + " horas (máximo esperado: " + maxHoras + ").");
        }
        if ("ERROR".equals(e.resultadoProgramado)) {
            alertas.add("Falló el último respaldo automático: " + e.detalleProgramado);
        }
        if ("ERROR".equals(e.resultadoVerificacion)) {
            alertas.add("El último respaldo NO pasó la verificación de restauración: " + e.detalleVerificacion);
        }
        if ("ERROR".equals(e.resultadoCopiaExterna)) {
            alertas.add("Falló la copia del respaldo fuera del servidor: " + e.detalleCopiaExterna);
        }
        if (!backupService.copiaExternaConfigurada()) {
            advertencias.add("No hay copia externa configurada: los respaldos están solo en este servidor.");
        }
        if ("INTEGRIDAD".equals(e.nivelVerificacion) && "OK".equals(e.resultadoVerificacion)) {
            advertencias.add("La verificación solo comprobó la integridad del archivo (no se pudo restaurar en una base temporal).");
        }

        return new EstadoRespaldoDTO(ultimo, horas, e.ultimaEjecucion, e.resultadoProgramado, e.detalleProgramado,
                e.fechaVerificacion, e.resultadoVerificacion, e.nivelVerificacion, e.detalleVerificacion,
                backupService.copiaExternaConfigurada(), e.fechaCopiaExterna, e.resultadoCopiaExterna, e.detalleCopiaExterna,
                alertas, advertencias, !alertas.isEmpty());
    }

    private File archivoEstado() {
        return new File(backupService.getBackupDir(), ARCHIVO_ESTADO);
    }

    private void guardarEstado(Estado e) {
        try {
            objectMapper.writeValue(archivoEstado(), e);
        } catch (Exception ex) {
            log.error("No se pudo guardar el estado del respaldo: {}", ex.getMessage());
        }
    }

    private Estado leerEstado() {
        File f = archivoEstado();
        if (!f.exists()) {
            return new Estado();
        }
        try {
            return objectMapper.readValue(f, Estado.class);
        } catch (Exception ex) {
            log.warn("Estado de respaldo ilegible, se ignora: {}", ex.getMessage());
            return new Estado();
        }
    }
}
