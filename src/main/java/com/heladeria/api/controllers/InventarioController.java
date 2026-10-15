package com.heladeria.api.controllers;

import com.heladeria.api.dto.inventario.*;
import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import com.heladeria.api.services.InventarioService;
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

@RestController
@RequestMapping("/api/inventario")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN') or hasRole('OWNER') or hasAuthority('PAGINA_stock')")
@Tag(name = "Inventario y Kardex", description = "Control integral de movimientos de stock, auditoría, valorización y kardex")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping("/movimientos")
    @Operation(summary = "Registrar un movimiento manual de inventario (Entrada, Merma, Ajuste Físico)")
    public ResponseEntity<MovimientoStockDTO> registrarMovimiento(
            @Valid @RequestBody RegistrarMovimientoDTO dto,
            Authentication authentication) {
        String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "ADMIN";
        MovimientoStockDTO resultado = inventarioService.registrarMovimientoManual(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @GetMapping("/kardex")
    @Operation(summary = "Consultar el histórico de movimientos del Kardex con filtros opcionales")
    public ResponseEntity<List<MovimientoStockDTO>> obtenerKardex(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(required = false) TipoItemStock tipoItem,
            @RequestParam(required = false) TipoMovimientoStock tipoMovimiento,
            @RequestParam(required = false) Long itemId) {
        return ResponseEntity.ok(inventarioService.obtenerKardex(inicio, fin, tipoItem, tipoMovimiento, itemId));
    }

    @GetMapping("/valorizacion")
    @Operation(summary = "Consultar el inventario físico valorado actual de productos, envases y helados")
    public ResponseEntity<ReporteValorizacionDTO> obtenerValorizacion() {
        return ResponseEntity.ok(inventarioService.obtenerValorizacion());
    }

    @GetMapping("/kpis")
    @Operation(summary = "Obtener KPIs consolidados de rotación, entradas, salidas y mermas de inventario")
    public ResponseEntity<KpisInventarioDTO> obtenerKpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(inventarioService.obtenerKpis(inicio, fin));
    }

    @GetMapping("/articulos")
    @Operation(summary = "Listar todos los artículos (productos simples, sabores, envases) para selección en formularios")
    public ResponseEntity<List<ArticuloInventarioDTO>> listarArticulos() {
        return ResponseEntity.ok(inventarioService.listarArticulosDisponibles());
    }

    @PostMapping("/tomas")
    @Operation(summary = "Crear y aplicar una toma de inventario físico con ajuste automático de existencias")
    public ResponseEntity<TomaInventarioDTO> crearTomaInventario(
            @Valid @RequestBody CrearTomaInventarioDTO dto,
            Authentication authentication) {
        String username = (authentication != null && authentication.getName() != null) ? authentication.getName() : "AUDITOR";
        TomaInventarioDTO resultado = inventarioService.crearYAplicarTomaInventario(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @GetMapping("/tomas")
    @Operation(summary = "Listar el histórico de auditorías y tomas de inventario físico")
    public ResponseEntity<List<TomaInventarioDTO>> listarTomasInventario() {
        return ResponseEntity.ok(inventarioService.listarTomasInventario());
    }

    @GetMapping("/tomas/{id}")
    @Operation(summary = "Obtener el reporte comparativo detallado de una toma de inventario físico (Sistema vs Físico)")
    public ResponseEntity<TomaInventarioDTO> obtenerTomaInventario(@PathVariable Long id) {
        return ResponseEntity.ok(inventarioService.obtenerTomaInventario(id));
    }
}
