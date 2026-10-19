package com.heladeria.api.services;

import com.heladeria.api.dto.auditoria.AuditoriaLogDTO;
import com.heladeria.api.dto.auditoria.ResumenAuditoriaDTO;
import com.heladeria.api.entities.AuditoriaLog;
import com.heladeria.api.repositories.AuditoriaLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditoriaService(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    /**
     * Registra un evento de auditoría en una transacción independiente.
     * En caso de error, registra el log de error sin interrumpir la operación de negocio principal.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarLog(String usuario, String modulo, String accion, Long entidadId, String entidadNombre, String detalle, String ipOrigen) {
        try {
            String userFinal = (usuario != null && !usuario.isBlank()) ? usuario : obtenerUsuarioActual();
            String ipFinal = (ipOrigen != null && !ipOrigen.isBlank()) ? ipOrigen : obtenerIpActual();

            AuditoriaLog audit = new AuditoriaLog(
                    userFinal,
                    modulo != null ? modulo.toUpperCase() : "GENERAL",
                    accion != null ? accion.toUpperCase() : "OPERACION",
                    entidadId,
                    entidadNombre,
                    detalle != null ? detalle : "-",
                    ipFinal
            );

            auditoriaLogRepository.save(audit);
            log.info("Auditoría registrada: [{}] {} en {} por {}", audit.getAccion(), audit.getEntidadNombre(), audit.getModulo(), audit.getUsuario());
        } catch (Exception ex) {
            log.error("Error no bloqueante registrando auditoría: {}", ex.getMessage());
        }
    }

    /**
     * Sobrecarga conveniente que autodetecta el usuario y la IP desde el contexto de la petición.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String modulo, String accion, Long entidadId, String entidadNombre, String detalle) {
        registrarLog(obtenerUsuarioActual(), modulo, accion, entidadId, entidadNombre, detalle, obtenerIpActual());
    }

    /**
     * Consulta filtrada de eventos de auditoría para la grilla y reportes.
     */
    @Transactional(readOnly = true)
    public List<AuditoriaLogDTO> listarLogs(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String usuario,
            String modulo,
            String accion,
            String buscar) {

        LocalDateTime desde = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = (fechaHasta != null) ? fechaHasta.atTime(LocalTime.MAX) : null;

        String userFiltro = (usuario != null && !usuario.isBlank() && !"TODOS".equalsIgnoreCase(usuario)) ? usuario.trim() : null;
        String modFiltro = (modulo != null && !modulo.isBlank() && !"TODOS".equalsIgnoreCase(modulo)) ? modulo.trim() : null;
        String accFiltro = (accion != null && !accion.isBlank() && !"TODOS".equalsIgnoreCase(accion)) ? accion.trim() : null;
        String buscarFiltro = (buscar != null && !buscar.isBlank()) ? buscar.trim() : null;

        List<AuditoriaLog> logs = auditoriaLogRepository.filtrarLogs(desde, hasta, userFiltro, modFiltro, accFiltro, buscarFiltro);

        return logs.stream().map(AuditoriaLogDTO::new).collect(Collectors.toList());
    }

    /**
     * Resumen de indicadores (KPIs) para la cabecera de la bitácora.
     */
    @Transactional(readOnly = true)
    public ResumenAuditoriaDTO obtenerResumen(
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String usuario,
            String modulo,
            String accion,
            String buscar) {

        List<AuditoriaLogDTO> logs = listarLogs(fechaDesde, fechaHasta, usuario, modulo, accion, buscar);

        long totalEventos = logs.size();
        long totalUsuarios = logs.stream().map(AuditoriaLogDTO::getUsuario).distinct().count();
        long totalModificaciones = logs.stream().filter(l -> "MODIFICACION".equalsIgnoreCase(l.getAccion())).count();
        long totalBajasOAnulaciones = logs.stream().filter(l ->
                "ELIMINACION".equalsIgnoreCase(l.getAccion()) ||
                "ANULACION".equalsIgnoreCase(l.getAccion()) ||
                "SALIDA_MERMA".equalsIgnoreCase(l.getAccion())
        ).count();

        return new ResumenAuditoriaDTO(totalEventos, totalUsuarios, totalModificaciones, totalBajasOAnulaciones);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerUsuariosAuditados() {
        return auditoriaLogRepository.findDistinctUsuarios();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerModulosAuditados() {
        return auditoriaLogRepository.findDistinctModulos();
    }

    @Transactional(readOnly = true)
    public List<String> obtenerAccionesAuditadas() {
        return auditoriaLogRepository.findDistinctAcciones();
    }

    private String obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "sistema";
    }

    private String obtenerIpActual() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwarded = request.getHeader("X-Forwarded-For");
                if (xForwarded != null && !xForwarded.isBlank()) {
                    return xForwarded.split(",")[0].trim();
                }
                String remoteAddr = request.getRemoteAddr();
                if (remoteAddr != null && !remoteAddr.isBlank()) {
                    return remoteAddr;
                }
            }
        } catch (Exception ignored) {
        }
        return "127.0.0.1";
    }
}
