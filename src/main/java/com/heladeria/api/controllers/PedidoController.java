package com.heladeria.api.controllers;

import com.heladeria.api.dto.ActualizarEstadoDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Pedidos y Ventas", description = "Procesamiento de ventas, validación de armado de helados y estados de pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @Operation(summary = "Listar ventas y pedidos con filtros opcionales de estado y fecha")
    public ResponseEntity<List<Pedido>> listar(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(pedidoService.listar(estado, fecha));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle completo de un pedido/venta por ID")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo pedido/venta con validaciones de heladería (límite de sabores, disponibilidad, etc.)")
    public ResponseEntity<Pedido> crear(@Valid @RequestBody PedidoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pedido (PENDIENTE, PREPARANDO, LISTO, ENTREGADO)")
    public ResponseEntity<Pedido> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDTO dto) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, dto.getNuevoEstado()));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido y reintegrar stock si corresponde")
    public ResponseEntity<Pedido> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelarPedido(id));
    }
}
