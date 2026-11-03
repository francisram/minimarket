package com.heladeria.api.controllers;

import com.heladeria.api.dto.*;
import com.heladeria.api.security.AuthUtils;
import com.heladeria.api.services.NotaCreditoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notas-credito")
@CrossOrigin(origins = "*")
@Tag(name = "Notas de crédito", description = "Anulación total o parcial de facturas emitidas")
public class NotaCreditoController {

    private final NotaCreditoService service;

    public NotaCreditoController(NotaCreditoService service) {
        this.service = service;
    }

    @GetMapping("/factura")
    @Operation(summary = "Buscar una factura por su número, con lo que aún se puede acreditar de cada línea")
    public ResponseEntity<FacturaParaNotaCreditoDTO> buscarFactura(@RequestParam String numero) {
        return ResponseEntity.ok(service.buscarFactura(numero));
    }

    @GetMapping("/pedido/{pedidoId}")
    @Operation(summary = "Estado de acreditación de la factura de un pedido")
    public ResponseEntity<FacturaParaNotaCreditoDTO> facturaDePedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(service.facturaDePedido(pedidoId));
    }

    @PostMapping("/pedido/{pedidoId}")
    @Operation(summary = "Emitir una nota de crédito (total o parcial) sobre una factura")
    public ResponseEntity<NotaCreditoDTO> emitir(@PathVariable Long pedidoId,
                                                 @Valid @RequestBody NotaCreditoRequestDTO request,
                                                 Authentication authentication) {
        String username = AuthUtils.requireUsername(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.emitir(pedidoId, request, username));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una nota de crédito")
    public ResponseEntity<NotaCreditoDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtener(id));
    }

    @GetMapping("/serie/{puntoId}")
    @Operation(summary = "Timbrado y numeración de las notas de crédito de un punto de expedición")
    public ResponseEntity<SerieNotaCreditoDTO> serie(@PathVariable Long puntoId) {
        return service.serie(puntoId).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/serie/{puntoId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Cargar o actualizar el timbrado de notas de crédito de un punto (solo ADMIN/OWNER)")
    public ResponseEntity<SerieNotaCreditoDTO> guardarSerie(@PathVariable Long puntoId, @Valid @RequestBody SerieNotaCreditoDTO dto) {
        return ResponseEntity.ok(service.guardarSerie(puntoId, dto));
    }
}
