package com.heladeria.api.controllers;

import com.heladeria.api.dto.CategoriaDTO;
import com.heladeria.api.dto.CategoriaRequestDTO;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_categorias') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_sabores') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Categorías", description = "Gestión integral de categorías dinámicas para heladería, cafetería y minimarket")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    @Operation(summary = "Listar categorías con filtros opcionales de texto, tipo de artículo y estado activo")
    public ResponseEntity<List<CategoriaDTO>> listar(
            @RequestParam(value = "filtro", required = false) String filtro,
            @RequestParam(value = "tipo", required = false) TipoArticulo tipo,
            @RequestParam(value = "soloActivos", required = false) Boolean soloActivos) {
        return ResponseEntity.ok(categoriaService.listar(filtro, tipo, soloActivos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoriaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_categorias') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_sabores')")
    @Operation(summary = "Crear nueva categoría")
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_categorias') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_sabores')")
    @Operation(summary = "Actualizar categoría existente")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_categorias') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_sabores')")
    @Operation(summary = "Cambiar estado activo/inactivo de la categoría")
    public ResponseEntity<CategoriaDTO> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean activo = body.getOrDefault("activo", true);
        return ResponseEntity.ok(categoriaService.cambiarEstado(id, activo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_categorias') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_sabores')")
    @Operation(summary = "Eliminar categoría")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
