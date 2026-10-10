package com.heladeria.api.controllers;

import com.heladeria.api.dto.*;
import com.heladeria.api.services.CajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caja")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_caja') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Control de Caja", description = "Gestión de turnos de caja, apertura, arqueo de ventas por método de pago y cierre")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping("/estado")
    @Operation(summary = "Consultar el estado actual de la caja del usuario autenticado")
    public ResponseEntity<EstadoCajaDTO> obtenerEstado(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cajaService.obtenerEstado(username));
    }

    @GetMapping("/resumen")
    @Operation(summary = "Consultar en tiempo real el arqueo acumulado por método de pago de la sesión indicada o la abierta del usuario")
    public ResponseEntity<ResumenCajaDTO> obtenerResumen(
            @RequestParam(required = false) Long sesionId,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cajaService.obtenerResumen(sesionId, username));
    }

    @GetMapping("/abiertas")
    @Operation(summary = "Listar todas las sesiones de caja que se encuentran actualmente abiertas")
    public ResponseEntity<List<SesionCajaDTO>> obtenerSesionesAbiertas() {
        return ResponseEntity.ok(cajaService.obtenerSesionesAbiertas());
    }

    @PostMapping("/abrir")
    @Operation(summary = "Abrir una nueva sesión de caja indicando el fondo inicial en Guaraníes")
    public ResponseEntity<SesionCajaDTO> abrir(
            @Valid @RequestBody AbrirCajaRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrirCaja(request, username));
    }

    @PostMapping("/cerrar")
    @Operation(summary = "Cerrar la sesión de caja actual declarando el dinero físico en mano y calculando diferencias")
    public ResponseEntity<SesionCajaDTO> cerrar(
            @Valid @RequestBody CerrarCajaRequestDTO request,
            Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(cajaService.cerrarCaja(request, username));
    }

    @GetMapping("/historial")
    @Operation(summary = "Consultar el historial completo de sesiones de caja ordenadas cronológicamente descendente")
    public ResponseEntity<List<SesionCajaDTO>> obtenerHistorial() {
        return ResponseEntity.ok(cajaService.obtenerHistorial());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle consolidado de una sesión de caja por ID")
    public ResponseEntity<SesionCajaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.obtenerPorId(id));
    }
}
