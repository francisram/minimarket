package com.heladeria.api.controllers;

import com.heladeria.api.dto.InstitucionDTO;
import com.heladeria.api.services.InstitucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/institucion", "/api/institucion"})
@CrossOrigin(origins = "*")
@Tag(name = "Institución", description = "Configuración del nombre y logo institucional para membretes de tickets")
public class InstitucionController {

    private final InstitucionService institucionService;

    public InstitucionController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    @GetMapping
    @Operation(summary = "Consultar datos y logo de la institución")
    public InstitucionDTO get() {
        return institucionService.get();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    @Operation(summary = "Actualizar datos y logo en base64 de la institución")
    public InstitucionDTO update(@RequestBody InstitucionDTO request) {
        return institucionService.update(request);
    }
}
