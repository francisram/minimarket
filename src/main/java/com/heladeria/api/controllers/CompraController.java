package com.heladeria.api.controllers;

import com.heladeria.api.dto.compras.CompraDTO;
import com.heladeria.api.dto.compras.RegistroCompraRequestDTO;
import com.heladeria.api.services.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_compras') or hasAuthority('PAGINA_proveedores') or hasAuthority('PAGINA_stock')")
@Tag(name = "Compras y Facturas de Proveedores", description = "Gestión de compras, facturas de proveedores, ingreso de mercadería a stock y liquidación de IVA")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping
    @Operation(summary = "Listar facturas de compra con filtros por proveedor, fechas y búsqueda")
    public ResponseEntity<List<CompraDTO>> listarCompras(
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) String buscar) {
        return ResponseEntity.ok(compraService.listarCompras(proveedorId, fechaDesde, fechaHasta, buscar));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener el detalle completo de una factura de compra por ID")
    public ResponseEntity<CompraDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    @PostMapping
    @Operation(summary = "Registrar una nueva factura de compra, ingresando stock y actualizando costos")
    public ResponseEntity<CompraDTO> registrarCompra(
            @Valid @RequestBody RegistroCompraRequestDTO dto,
            Authentication authentication) {
        String usuario = (authentication != null && authentication.getName() != null)
                ? authentication.getName() : "ADMIN";
        CompraDTO creada = compraService.registrarCompra(dto, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular una factura de compra y revertir el stock de los artículos")
    public ResponseEntity<CompraDTO> anularCompra(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        String motivo = (body != null && body.containsKey("motivo")) ? body.get("motivo") : null;
        String usuario = (authentication != null && authentication.getName() != null)
                ? authentication.getName() : "ADMIN";
        CompraDTO anulada = compraService.anularCompra(id, motivo, usuario);
        return ResponseEntity.ok(anulada);
    }
}
