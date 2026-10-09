package com.heladeria.api.controllers;

import com.heladeria.api.dto.ImpresoraDTO;
import com.heladeria.api.dto.ImpresoraRequestDTO;
import com.heladeria.api.dto.ImprimirRequestDTO;
import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.services.ImpresionService;
import com.heladeria.api.services.ImpresoraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/impresoras", "/api/impresoras"})
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
@Tag(name = "Impresoras", description = "Administración de impresoras térmicas ESC/POS (RED y CUPS) y pruebas de impresión")
public class ImpresoraController {

    private final ImpresoraService impresoraService;
    private final ImpresionService impresionService;
    private final InstitucionRepository institucionRepository;

    public ImpresoraController(ImpresoraService impresoraService,
                               ImpresionService impresionService,
                               InstitucionRepository institucionRepository) {
        this.impresoraService = impresoraService;
        this.impresionService = impresionService;
        this.institucionRepository = institucionRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todas las impresoras configuradas")
    public List<ImpresoraDTO> findAll() {
        return impresoraService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener impresora por ID")
    public ImpresoraDTO findById(@PathVariable Long id) {
        return impresoraService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Registrar nueva impresora (RED o CUPS)")
    public ResponseEntity<ImpresoraDTO> create(@Valid @RequestBody ImpresoraRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impresoraService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar configuración de una impresora existente")
    public ImpresoraDTO update(@PathVariable Long id, @Valid @RequestBody ImpresoraRequestDTO request) {
        return impresoraService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar impresora")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        impresoraService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/imprimir")
    @Operation(summary = "Enviar impresión de prueba con texto plano y logo opcional")
    public ResponseEntity<Void> imprimir(@PathVariable Long id, @Valid @RequestBody ImprimirRequestDTO request) {
        Impresora impresora = impresoraService.getOrThrow(id);
        String logoBase64 = request.isIncluirLogo() ? logoInstitucion() : null;
        impresionService.imprimir(impresora, request.getTexto(), logoBase64);
        return ResponseEntity.ok().build();
    }

    private String logoInstitucion() {
        return institucionRepository.findById(1L)
                .map(Institucion::getLogoBase64)
                .orElse(null);
    }
}
