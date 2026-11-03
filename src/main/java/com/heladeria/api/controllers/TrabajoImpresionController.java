package com.heladeria.api.controllers;

import com.heladeria.api.entities.TrabajoImpresion;
import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import com.heladeria.api.services.TrabajoImpresionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/impresion/trabajos")
@Tag(name = "Spooler de impresión", description = "Cola de impresión con reintentos y dead letter")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_impresoras') or hasAuthority('PAGINA_pos') or hasAuthority('PAGINA_pedidos') or hasAuthority('PAGINA_caja')")
public class TrabajoImpresionController {

    private final TrabajoImpresionService service;

    public TrabajoImpresionController(TrabajoImpresionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar trabajos de impresión (opcionalmente por estado)")
    public List<TrabajoImpresion> listar(@RequestParam(required = false) EstadoTrabajoImpresion estado) {
        return service.listar(estado);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Cantidad de trabajos por estado; MUERTO indica tickets sin imprimir")
    public Map<EstadoTrabajoImpresion, Long> resumen() {
        return service.resumen();
    }

    @PostMapping("/{id}/reintentar")
    @Operation(summary = "Reencolar un trabajo en dead letter")
    public ResponseEntity<TrabajoImpresion> reintentar(@PathVariable Long id) {
        return ResponseEntity.ok(service.reintentar(id));
    }
}
