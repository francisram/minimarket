package com.heladeria.api.controllers;

import com.heladeria.api.dto.PaginaDTO;
import com.heladeria.api.services.PaginaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/paginas")
@CrossOrigin(origins = "*")
@Tag(name = "Páginas y Módulos", description = "Catálogo de páginas del sistema y páginas disponibles según la licencia activa")
public class PaginaController {

    private final PaginaService paginaService;

    public PaginaController(PaginaService paginaService) {
        this.paginaService = paginaService;
    }

    // Catalogo completo del sistema: solo OWNER o quien administre licencias
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasAuthority('PAGINA_licencia')")
    @Operation(summary = "Listar catálogo completo de módulos del sistema")
    public ResponseEntity<List<PaginaDTO>> listar() {
        return ResponseEntity.ok(paginaService.listarTodas());
    }

    // Solo los modulos incluidos en el plan activo: para configurar roles
    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_roles')")
    @Operation(summary = "Listar páginas disponibles según el plan de licencia contratado")
    public ResponseEntity<List<PaginaDTO>> listarDisponibles() {
        return ResponseEntity.ok(paginaService.listarDisponibles());
    }
}
