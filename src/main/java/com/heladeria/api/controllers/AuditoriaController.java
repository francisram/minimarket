package com.heladeria.api.controllers;

import com.heladeria.api.dto.auditoria.AuditoriaLogDTO;
import com.heladeria.api.dto.auditoria.ResumenAuditoriaDTO;
import com.heladeria.api.services.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_auditoria') or hasAuthority('PAGINA_bitacora')")
@Tag(name = "Bitácora Centralizada de Auditoría", description = "Trazabilidad de cambios, modificaciones de precios, stock, compras y operaciones de usuarios")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar eventos de auditoría con filtros por fecha, usuario, módulo, acción y texto libre")
    public ResponseEntity<List<AuditoriaLogDTO>> listarLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String buscar) {
        return ResponseEntity.ok(auditoriaService.listarLogs(fechaDesde, fechaHasta, usuario, modulo, accion, buscar));
    }

    @GetMapping("/resumen")
    @Operation(summary = "Obtener métricas y KPIs de eventos de auditoría para el filtro actual")
    public ResponseEntity<ResumenAuditoriaDTO> obtenerResumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String buscar) {
        return ResponseEntity.ok(auditoriaService.obtenerResumen(fechaDesde, fechaHasta, usuario, modulo, accion, buscar));
    }

    @GetMapping("/usuarios")
    @Operation(summary = "Obtener lista única de usuarios con actividad registrada en la bitácora")
    public ResponseEntity<List<String>> obtenerUsuarios() {
        return ResponseEntity.ok(auditoriaService.obtenerUsuariosAuditados());
    }

    @GetMapping("/modulos")
    @Operation(summary = "Obtener lista única de módulos con actividad registrada")
    public ResponseEntity<List<String>> obtenerModulos() {
        return ResponseEntity.ok(auditoriaService.obtenerModulosAuditados());
    }

    @GetMapping("/acciones")
    @Operation(summary = "Obtener lista única de acciones registradas")
    public ResponseEntity<List<String>> obtenerAcciones() {
        return ResponseEntity.ok(auditoriaService.obtenerAccionesAuditadas());
    }
}
