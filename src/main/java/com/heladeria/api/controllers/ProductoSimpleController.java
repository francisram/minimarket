package com.heladeria.api.controllers;

import com.heladeria.api.dto.ProductoSimpleRequestDTO;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.services.ProductoSimpleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos-simples")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos') or hasAuthority('PAGINA_proveedores') or hasAuthority('PAGINA_pedidos')")
@Tag(name = "Productos Simples", description = "Bebidas, cafetería y helados envasados que no requieren armado")
public class ProductoSimpleController {

    private final ProductoSimpleService productoSimpleService;

    public ProductoSimpleController(ProductoSimpleService productoSimpleService) {
        this.productoSimpleService = productoSimpleService;
    }

    @GetMapping
    @Operation(summary = "Listar productos simples con filtro por categoría o activos")
    public ResponseEntity<List<ProductoSimple>> listar(
            @RequestParam(defaultValue = "false") boolean soloActivos,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(productoSimpleService.listar(soloActivos, categoria));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto simple por ID")
    public ResponseEntity<ProductoSimple> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoSimpleService.obtenerPorId(id));
    }

    @GetMapping("/codigo-barra/{codigo}")
    @Operation(summary = "Buscar producto simple por su código de barra (para lector en POS)")
    public ResponseEntity<ProductoSimple> buscarPorCodigoBarra(@PathVariable String codigo) {
        return ResponseEntity.ok(productoSimpleService.buscarPorCodigoBarra(codigo));
    }

    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Listar productos simples asociados a un proveedor")
    public ResponseEntity<List<ProductoSimple>> listarPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(productoSimpleService.listarPorProveedor(proveedorId));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Crear nuevo producto simple")
    public ResponseEntity<ProductoSimple> crear(@Valid @RequestBody ProductoSimpleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoSimpleService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Actualizar producto simple")
    public ResponseEntity<ProductoSimple> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoSimpleRequestDTO dto) {
        return ResponseEntity.ok(productoSimpleService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_productos')")
    @Operation(summary = "Eliminar producto simple")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoSimpleService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
