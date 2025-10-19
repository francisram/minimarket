package com.sanmartin.minimarketbase.controllers;

import com.sanmartin.minimarketbase.VentaService;
import com.sanmartin.minimarketbase.dto.VentaRequest;
import com.sanmartin.minimarketbase.entities.Venta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*") // Para permitir requests desde Postman
public class VentaController {

    @Autowired
    private VentaService ventaService;

    /**
     * Procesar una nueva venta
     * POST http://localhost:8080/api/ventas
     */
    @PostMapping
    public ResponseEntity<?> procesarVenta(@RequestBody VentaRequest ventaRequest) {
        try {
            Venta venta = ventaService.procesarVenta(ventaRequest);
            return ResponseEntity.ok(venta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("ERROR", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                new ErrorResponse("ERROR_INTERNO", "Error interno del servidor")
            );
        }
    }

    /**
     * Obtener todas las ventas
     * GET http://localhost:8080/api/ventas
     */
    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodasLasVentas() {
        List<Venta> ventas = ventaService.obtenerTodasLasVentas();
        return ResponseEntity.ok(ventas);
    }

    /**
     * Obtener una venta por ID
     * GET http://localhost:8080/api/ventas/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Integer id) {
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            return ResponseEntity.ok(venta);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cancelar una venta
     * PUT http://localhost:8080/api/ventas/1/cancelar
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarVenta(@PathVariable Integer id) {
        try {
            Venta ventaCancelada = ventaService.cancelarVenta(id);
            return ResponseEntity.ok(ventaCancelada);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("ERROR", e.getMessage())
            );
        }
    }

    // Clase interna para respuestas de error
    public static class ErrorResponse {
        private String codigo;
        private String mensaje;

        public ErrorResponse(String codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }

        // Getters y Setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    }
}