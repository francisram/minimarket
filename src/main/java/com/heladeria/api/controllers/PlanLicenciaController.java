package com.heladeria.api.controllers;

import com.heladeria.api.dto.PlanLicenciaDTO;
import com.heladeria.api.services.PlanLicenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes-licencia")
@CrossOrigin(origins = "*")
@Tag(name = "Planes de Licencia", description = "Administración de tiers y planes de licenciamiento")
public class PlanLicenciaController {

    private final PlanLicenciaService planLicenciaService;

    public PlanLicenciaController(PlanLicenciaService planLicenciaService) {
        this.planLicenciaService = planLicenciaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasAuthority('PAGINA_licencia')")
    @Operation(summary = "Listar planes de licencia existentes")
    public ResponseEntity<List<PlanLicenciaDTO>> listar() {
        return ResponseEntity.ok(planLicenciaService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasAuthority('PAGINA_licencia')")
    @Operation(summary = "Obtener plan de licencia por ID")
    public ResponseEntity<PlanLicenciaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planLicenciaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Crear nuevo plan de licencia (Solo OWNER)")
    public ResponseEntity<PlanLicenciaDTO> crear(@Valid @RequestBody PlanLicenciaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planLicenciaService.guardar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Actualizar plan de licencia (Solo OWNER)")
    public ResponseEntity<PlanLicenciaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PlanLicenciaDTO dto) {
        return ResponseEntity.ok(planLicenciaService.actualizar(id, dto));
    }
}
