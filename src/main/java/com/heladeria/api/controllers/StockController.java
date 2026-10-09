package com.heladeria.api.controllers;

import com.heladeria.api.dto.stock.AjusteStockDTO;
import com.heladeria.api.dto.stock.StockAlertasResponseDTO;
import com.heladeria.api.entities.Presentacion;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.Sabor;
import com.heladeria.api.services.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
@Tag(name = "Stock e Inventario", description = "Control de existencias, umbrales mínimos y alertas para productos, sabores y envases")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_stock') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_pedidos')")
    @Operation(summary = "Obtener alertas consolidadas de stock bajo o agotado para productos, sabores y envases")
    public ResponseEntity<StockAlertasResponseDTO> obtenerAlertas() {
        return ResponseEntity.ok(stockService.obtenerAlertasStock());
    }

    @PatchMapping("/productos/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_stock') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Ajustar stock o stock mínimo de un producto simple")
    public ResponseEntity<ProductoSimple> ajustarStockProducto(
            @PathVariable Long id,
            @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(stockService.actualizarStockProducto(id, dto));
    }

    @PatchMapping("/sabores/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_stock') or hasAuthority('PAGINA_sabores')")
    @Operation(summary = "Ajustar stock en kilos o stock mínimo de un sabor")
    public ResponseEntity<Sabor> ajustarStockSabor(
            @PathVariable Long id,
            @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(stockService.actualizarStockSabor(id, dto));
    }

    @PatchMapping("/presentaciones/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_stock') or hasAuthority('PAGINA_presentaciones')")
    @Operation(summary = "Ajustar stock de envases/cucuruchos o stock mínimo de una presentación")
    public ResponseEntity<Presentacion> ajustarStockPresentacion(
            @PathVariable Long id,
            @Valid @RequestBody AjusteStockDTO dto) {
        return ResponseEntity.ok(stockService.actualizarStockPresentacion(id, dto));
    }
}
