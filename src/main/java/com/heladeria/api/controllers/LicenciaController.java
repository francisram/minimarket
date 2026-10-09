package com.heladeria.api.controllers;

import com.heladeria.api.dto.LicenciaDTO;
import com.heladeria.api.services.LicenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/licencia")
@CrossOrigin(origins = "*")
@Tag(name = "Licencia", description = "Gestión de la licencia y contrato activo de esta instalación")
public class LicenciaController {

    private final LicenciaService licenciaService;

    public LicenciaController(LicenciaService licenciaService) {
        this.licenciaService = licenciaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasAuthority('PAGINA_licencia')")
    @Operation(summary = "Obtener el plan de licencia actualmente activo en esta instalación")
    public ResponseEntity<LicenciaDTO> obtener() {
        return ResponseEntity.ok(licenciaService.obtener());
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Actualizar el plan de licencia activo o su fecha de vencimiento (Solo OWNER)")
    public ResponseEntity<LicenciaDTO> actualizar(@Valid @RequestBody LicenciaDTO dto) {
        return ResponseEntity.ok(licenciaService.actualizar(dto));
    }
}
