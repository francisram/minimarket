package com.heladeria.api.controllers;

import com.heladeria.api.dto.RolDTO;
import com.heladeria.api.services.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@Tag(name = "Roles", description = "Gestión de roles y asignación de páginas autorizadas")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    // Abierto a cualquier usuario autenticado: el frontend lo consulta para conocer
    // las paginas que le tocan a su rol al loguear.
    @GetMapping
    @Operation(summary = "Listar roles (abierto a todo usuario autenticado)")
    public ResponseEntity<List<RolDTO>> listar() {
        return ResponseEntity.ok(rolService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_roles')")
    @Operation(summary = "Obtener rol por ID")
    public ResponseEntity<RolDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rolService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_roles')")
    @Operation(summary = "Crear nuevo rol con validación contra el plan de licencia activo")
    public ResponseEntity<RolDTO> crear(@Valid @RequestBody RolDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.guardar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_roles')")
    @Operation(summary = "Actualizar rol y sus páginas permitidas")
    public ResponseEntity<RolDTO> actualizar(@PathVariable Long id, @Valid @RequestBody RolDTO dto) {
        return ResponseEntity.ok(rolService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_roles')")
    @Operation(summary = "Eliminar rol")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
