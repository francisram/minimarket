package com.heladeria.api.controllers;

import com.heladeria.api.dto.ProveedorDTO;
import com.heladeria.api.dto.ProveedorRequestDTO;
import com.heladeria.api.services.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_proveedores') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_stock')")
@Tag(name = "Proveedores", description = "Gestión integral de proveedores, compras y vinculación de productos")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores con filtro opcional de texto o activos")
    public ResponseEntity<List<ProveedorDTO>> listar(
            @RequestParam(value = "filtro", required = false) String filtro,
            @RequestParam(value = "soloActivos", required = false) Boolean soloActivos) {
        return ResponseEntity.ok(proveedorService.listar(filtro, soloActivos));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar proveedor exacto por RUC / Cédula")
    public ResponseEntity<ProveedorDTO> buscarPorRuc(@RequestParam("ruc") String ruc) {
        return proveedorService.buscarPorRuc(ruc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de proveedor por ID")
    public ResponseEntity<ProveedorDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo proveedor")
    public ResponseEntity<ProveedorDTO> crear(@Valid @RequestBody ProveedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de proveedor")
    public ResponseEntity<ProveedorDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO dto) {
        return ResponseEntity.ok(proveedorService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado activo/inactivo de proveedor")
    public ResponseEntity<ProveedorDTO> cambiarEstado(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        return ResponseEntity.ok(proveedorService.cambiarEstado(id, activo));
    }
}
