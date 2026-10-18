package com.heladeria.api.controllers;

import com.heladeria.api.dto.AsignarProveedorRequestDTO;
import com.heladeria.api.dto.ProductoDTO;
import com.heladeria.api.dto.ProductoRequestDTO;
import com.heladeria.api.entities.enums.TipoArticulo;
import com.heladeria.api.services.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_proveedores') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Catálogo Maestro de Productos", description = "Gestión centralizada de artículos de retail: venta directa, envases, sabores, toppings e insumos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos con filtros opcionales (tipo, proveedor, activos o búsqueda libre)")
    public ResponseEntity<List<ProductoDTO>> listar(
            @RequestParam(required = false) TipoArticulo tipo,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(defaultValue = "false") Boolean soloActivos,
            @RequestParam(required = false) String buscar) {
        return ResponseEntity.ok(productoService.listar(tipo, proveedorId, soloActivos, buscar));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/codigo-barra/{codigo}")
    @Operation(summary = "Buscar producto por código de barra para lector de código de barras")
    public ResponseEntity<ProductoDTO> buscarPorCodigoBarra(@PathVariable String codigo) {
        return ResponseEntity.ok(productoService.buscarPorCodigoBarra(codigo));
    }

    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Listar todos los productos abastecidos por un proveedor específico")
    public ResponseEntity<List<ProductoDTO>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(productoService.listarPorProveedor(proveedorId));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Crear nuevo producto en el catálogo maestro")
    public ResponseEntity<ProductoDTO> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Actualizar producto existente y sus proveedores asociados")
    public ResponseEntity<ProductoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Eliminar producto del catálogo maestro")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Activar o desactivar producto")
    public ResponseEntity<ProductoDTO> cambiarEstado(@PathVariable Long id, @RequestParam boolean activo) {
        return ResponseEntity.ok(productoService.cambiarEstado(id, activo));
    }

    @PostMapping("/{id}/proveedores")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_proveedores')")
    @Operation(summary = "Asociar un proveedor con costo de compra y referencia al producto")
    public ResponseEntity<ProductoDTO> asociarProveedor(@PathVariable Long id, @Valid @RequestBody AsignarProveedorRequestDTO dto) {
        return ResponseEntity.ok(productoService.asociarProveedor(id, dto));
    }

    @DeleteMapping("/{id}/proveedores/{proveedorId}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_proveedores')")
    @Operation(summary = "Desasociar un proveedor del producto")
    public ResponseEntity<ProductoDTO> desasociarProveedor(@PathVariable Long id, @PathVariable Long proveedorId) {
        return ResponseEntity.ok(productoService.desasociarProveedor(id, proveedorId));
    }
}
