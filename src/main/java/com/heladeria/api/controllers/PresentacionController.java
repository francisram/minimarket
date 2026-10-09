package com.heladeria.api.controllers;

import com.heladeria.api.dto.PresentacionRequestDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.services.PresentacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presentaciones")
@CrossOrigin(origins = "*")
@Tag(name = "Presentaciones", description = "Formatos y envases de helado (potes, cucuruchos, vasos) y límites de sabores")
public class PresentacionController {

    private final PresentacionService presentacionService;

    public PresentacionController(PresentacionService presentacionService) {
        this.presentacionService = presentacionService;
    }

    @GetMapping
    @Operation(summary = "Listar presentaciones (formatos de helado)")
    public ResponseEntity<List<Presentacion>> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return ResponseEntity.ok(presentacionService.listar(soloActivas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener presentación por ID")
    public ResponseEntity<Presentacion> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(presentacionService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo formato de helado")
    public ResponseEntity<Presentacion> crear(@Valid @RequestBody PresentacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presentacionService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar presentación")
    public ResponseEntity<Presentacion> actualizar(@PathVariable Long id, @Valid @RequestBody PresentacionRequestDTO dto) {
        return ResponseEntity.ok(presentacionService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar presentación")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        presentacionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
