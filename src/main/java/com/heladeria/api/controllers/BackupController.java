package com.heladeria.api.controllers;

import com.heladeria.api.dto.BackupInfoDTO;
import com.heladeria.api.dto.BackupResponseDTO;
import com.heladeria.api.dto.EstadoRespaldoDTO;
import com.heladeria.api.services.BackupAutomaticoService;
import com.heladeria.api.services.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/backup")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_backup')")
@Tag(name = "Copias de Seguridad", description = "Gestión de respaldos y restauración integral de base de datos PostgreSQL")
public class BackupController {

    private final BackupService backupService;
    private final BackupAutomaticoService backupAutomaticoService;

    public BackupController(BackupService backupService, BackupAutomaticoService backupAutomaticoService) {
        this.backupService = backupService;
        this.backupAutomaticoService = backupAutomaticoService;
    }

    @GetMapping("/estado")
    @Operation(summary = "Estado de salud de los respaldos: último respaldo, verificación, copia externa y alertas")
    public EstadoRespaldoDTO estado() {
        return backupAutomaticoService.obtenerEstado();
    }

    @PostMapping("/verificar/{nombreArchivo}")
    @Operation(summary = "Verificar un respaldo existente con una restauración de prueba en una base temporal")
    public BackupService.ResultadoVerificacion verificar(@PathVariable String nombreArchivo) {
        return backupService.verificarRespaldo(nombreArchivo);
    }

    @PostMapping("/crear")
    @Operation(summary = "Generar una nueva copia de seguridad completa (.dump)")
    public ResponseEntity<BackupResponseDTO> crear() {
        BackupInfoDTO backupInfo = backupService.crearRespaldo();
        return ResponseEntity.ok(new BackupResponseDTO(true, "Copia de seguridad generada exitosamente.", backupInfo));
    }

    @GetMapping("/listar")
    @Operation(summary = "Listar todas las copias de seguridad disponibles en el servidor")
    public List<BackupInfoDTO> listar() {
        return backupService.listarRespaldos();
    }

    @GetMapping("/descargar/{nombreArchivo}")
    @Operation(summary = "Descargar un archivo de respaldo específico")
    public ResponseEntity<Resource> descargar(@PathVariable String nombreArchivo) {
        Resource recurso = backupService.obtenerArchivo(nombreArchivo);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }

    @DeleteMapping("/{nombreArchivo}")
    @Operation(summary = "Eliminar un archivo de respaldo del servidor")
    public ResponseEntity<Void> eliminar(@PathVariable String nombreArchivo) {
        backupService.eliminarRespaldo(nombreArchivo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/restaurar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Restaurar la base de datos a partir de un archivo subido (.dump)")
    public ResponseEntity<BackupResponseDTO> restaurar(@RequestParam("archivo") MultipartFile archivo) {
        backupService.restaurarDesdeArchivo(archivo);
        return ResponseEntity.ok(new BackupResponseDTO(true, "Base de datos restaurada exitosamente."));
    }

    @PostMapping("/restaurar-servidor/{nombreArchivo}")
    @Operation(summary = "Restaurar la base de datos utilizando un archivo de respaldo del servidor")
    public ResponseEntity<BackupResponseDTO> restaurarServidor(@PathVariable String nombreArchivo) {
        backupService.restaurarDesdeServidor(nombreArchivo);
        return ResponseEntity.ok(new BackupResponseDTO(true, "Base de datos restaurada exitosamente desde el servidor."));
    }
}
