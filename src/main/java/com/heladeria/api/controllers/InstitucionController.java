package com.heladeria.api.controllers;

import com.heladeria.api.dto.InstitucionDTO;
import com.heladeria.api.services.InstitucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public InstitucionDTO get(org.springframework.security.core.Authentication authentication) {
        InstitucionDTO completa = institucionService.get();
        boolean autenticado = authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
        // Sin sesión (pantalla de login) solo se publican el nombre y el logo: RUC, timbrado y dirección no
        return autenticado ? completa : new InstitucionDTO(completa.getNombre(), completa.getLogoBase64());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_institucion')")
    @Operation(summary = "Actualizar datos y logo en base64 de la institución")
    public InstitucionDTO update(@Valid @RequestBody InstitucionDTO request) {
        return institucionService.update(request);
    }
}
