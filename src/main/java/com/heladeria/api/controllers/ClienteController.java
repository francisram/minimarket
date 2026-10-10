package com.heladeria.api.controllers;

import com.heladeria.api.dto.ClienteDTO;
import com.heladeria.api.dto.ClienteRequestDTO;
import com.heladeria.api.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_clientes') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Clientes", description = "Gestión integral de clientes, autocompletado y búsqueda por RUC")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    @Operation(summary = "Listar clientes con opción de filtro por RUC o Razón Social")
    public ResponseEntity<List<ClienteDTO>> listar(@RequestParam(value = "filtro", required = false) String filtro) {
        return ResponseEntity.ok(clienteService.listar(filtro));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar cliente exacto por RUC / Cédula")
    public ResponseEntity<ClienteDTO> buscarPorRuc(@RequestParam("ruc") String ruc) {
        return clienteService.buscarPorRuc(ruc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de cliente por ID")
    public ResponseEntity<ClienteDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo cliente")
    public ResponseEntity<ClienteDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de cliente")
    public ResponseEntity<ClienteDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado activo/inactivo de cliente")
    public ResponseEntity<ClienteDTO> cambiarEstado(@PathVariable Long id, @RequestParam("activo") boolean activo) {
        return ResponseEntity.ok(clienteService.cambiarEstado(id, activo));
    }
}
