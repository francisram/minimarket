package com.heladeria.api.controllers;

import com.heladeria.api.dto.DisponibilidadDTO;
import com.heladeria.api.dto.SaborRequestDTO;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.services.SaborService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sabores")
@CrossOrigin(origins = "*")
@Tag(name = "Sabores", description = "Gestión del catálogo de sabores de helado, dietas y disponibilidad")
public class SaborController {

    private final SaborService saborService;

    public SaborController(SaborService saborService) {
        this.saborService = saborService;
    }

    @GetMapping
    @Operation(summary = "Listar sabores con filtros opcionales (categoría, apto celíaco, vegano, sin azúcar, disponibilidad)")
    public ResponseEntity<List<Sabor>> listar(
            @RequestParam(required = false) Boolean soloDisponibles,
            @RequestParam(required = false) CategoriaSabor categoria,
            @RequestParam(required = false) Boolean celiaco,
            @RequestParam(required = false) Boolean vegano,
            @RequestParam(required = false) Boolean sinAzucar) {
        return ResponseEntity.ok(saborService.listar(soloDisponibles, categoria, celiaco, vegano, sinAzucar));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sabor por ID")
    public ResponseEntity<Sabor> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(saborService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo sabor de helado")
    public ResponseEntity<Sabor> crear(@Valid @RequestBody SaborRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saborService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sabor existente")
    public ResponseEntity<Sabor> actualizar(@PathVariable Long id, @Valid @RequestBody SaborRequestDTO dto) {
        return ResponseEntity.ok(saborService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/disponibilidad")
    @Operation(summary = "Cambiar disponibilidad en mostrador (disponible/agotado)")
    public ResponseEntity<Sabor> cambiarDisponibilidad(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadDTO dto) {
        return ResponseEntity.ok(saborService.cambiarDisponibilidad(id, dto.getDisponible()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sabor por ID")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        saborService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
