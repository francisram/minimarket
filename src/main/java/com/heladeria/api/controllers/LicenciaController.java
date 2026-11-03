package com.heladeria.api.controllers;

import com.heladeria.api.dto.LicenciaDTO;
import com.heladeria.api.dto.LicenciaEstadoDTO;
import com.heladeria.api.services.ControlRelojService;
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
    private final ControlRelojService controlRelojService;

    public LicenciaController(LicenciaService licenciaService, ControlRelojService controlRelojService) {
        this.licenciaService = licenciaService;
        this.controlRelojService = controlRelojService;
    }

    @GetMapping("/estado")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Estado informativo de la licencia (vigente, por vencer, vencida) y alerta de retroceso de reloj")
    public ResponseEntity<LicenciaEstadoDTO> estado() {
        return ResponseEntity.ok(licenciaService.obtenerEstado());
    }

    @PostMapping("/reloj/reconocer")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Reconocer la alerta de retroceso de reloj una vez corregida la hora del servidor (Solo OWNER)")
    public ResponseEntity<LicenciaEstadoDTO> reconocerReloj(org.springframework.security.core.Authentication authentication) {
        controlRelojService.reconocerRetroceso(com.heladeria.api.security.AuthUtils.requireUsername(authentication));
        return ResponseEntity.ok(licenciaService.obtenerEstado());
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
