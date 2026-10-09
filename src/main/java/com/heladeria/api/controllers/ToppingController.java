package com.heladeria.api.controllers;

import com.heladeria.api.dto.DisponibilidadDTO;
import com.heladeria.api.dto.ToppingRequestDTO;
import com.heladeria.api.entities.Topping;
import com.heladeria.api.services.ToppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/toppings")
@CrossOrigin(origins = "*")
@Tag(name = "Toppings y Agregados", description = "Salsas, baños y adicionales para helados")
public class ToppingController {

    private final ToppingService toppingService;

    public ToppingController(ToppingService toppingService) {
        this.toppingService = toppingService;
    }

    @GetMapping
    @Operation(summary = "Listar toppings y agregados")
    public ResponseEntity<List<Topping>> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
        return ResponseEntity.ok(toppingService.listar(soloDisponibles));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener topping por ID")
    public ResponseEntity<Topping> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toppingService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo topping o adicional")
    public ResponseEntity<Topping> crear(@Valid @RequestBody ToppingRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(toppingService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar topping")
    public ResponseEntity<Topping> actualizar(@PathVariable Long id, @Valid @RequestBody ToppingRequestDTO dto) {
        return ResponseEntity.ok(toppingService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/disponibilidad")
    @Operation(summary = "Cambiar disponibilidad de topping")
    public ResponseEntity<Topping> cambiarDisponibilidad(
            @PathVariable Long id,
            @Valid @RequestBody DisponibilidadDTO dto) {
        return ResponseEntity.ok(toppingService.cambiarDisponibilidad(id, dto.getDisponible()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar topping")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        toppingService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
