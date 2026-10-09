package com.heladeria.api.controllers;

import com.heladeria.api.dto.UsuarioDTO;
import com.heladeria.api.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_usuarios')")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema y credenciales")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<UsuarioDTO> crear(@Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
