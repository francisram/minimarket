package com.heladeria.api.controllers;

import com.heladeria.api.dto.PuntoExpedicionDTO;
import com.heladeria.api.dto.PuntoExpedicionRequestDTO;
import com.heladeria.api.services.PuntoExpedicionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.heladeria.api.security.AuthUtils;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/puntos-expedicion")
@CrossOrigin(origins = "*")
@Tag(name = "Puntos de Expedición", description = "Gestión de puntos de expedición y terminales de venta según normativa fiscal de la DNIT")
public class PuntoExpedicionController {

    private final PuntoExpedicionService puntoExpedicionService;

    public PuntoExpedicionController(PuntoExpedicionService puntoExpedicionService) {
        this.puntoExpedicionService = puntoExpedicionService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los puntos de expedición con su estado de uso actual")
    public ResponseEntity<List<PuntoExpedicionDTO>> listarTodos() {
        return ResponseEntity.ok(puntoExpedicionService.listarTodos());
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Listar los puntos de expedición activos que no están ocupados por ninguna sesión de caja abierta")
    public ResponseEntity<List<PuntoExpedicionDTO>> listarDisponibles() {
        return ResponseEntity.ok(puntoExpedicionService.listarDisponibles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle de un punto de expedición por su ID")
    public ResponseEntity<PuntoExpedicionDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(puntoExpedicionService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_institucion')")
    @Operation(summary = "Crear un nuevo punto de expedición / terminal de caja fiscal")
    public ResponseEntity<PuntoExpedicionDTO> crear(
            @Valid @RequestBody PuntoExpedicionRequestDTO request,
            Authentication authentication) {
        String username = AuthUtils.requireUsername(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(puntoExpedicionService.crear(request, username));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_institucion')")
    @Operation(summary = "Actualizar configuración y correlativos de un punto de expedición existente")
    public ResponseEntity<PuntoExpedicionDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PuntoExpedicionRequestDTO request,
            Authentication authentication) {
        String username = AuthUtils.requireUsername(authentication);
        return ResponseEntity.ok(puntoExpedicionService.actualizar(id, request, username));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_institucion')")
    @Operation(summary = "Activar o desactivar un punto de expedición")
    public ResponseEntity<PuntoExpedicionDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {
        Boolean activo = body.getOrDefault("activo", true);
        String username = AuthUtils.requireUsername(authentication);
        return ResponseEntity.ok(puntoExpedicionService.cambiarEstado(id, activo, username));
    }
}
