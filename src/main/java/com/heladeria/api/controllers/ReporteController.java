package com.heladeria.api.controllers;

import com.heladeria.api.dto.reportes.*;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasAuthority('PAGINA_reportes')")
@Tag(name = "Reportes y Estadísticas", description = "Módulo de reportería analítica, KPIs de dashboard, libro de ventas fiscal IVA (SET/DNIT), arqueos y rankings")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener KPIs ejecutivos, evolución diaria y métricas globales para el dashboard")
    public ResponseEntity<DashboardReporteDTO> obtenerDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerDashboard(fechaInicio, fechaFin));
    }

    @GetMapping("/ventas")
    @Operation(summary = "Consultar reporte detallado de ventas con filtros de fecha, pago, comprobante y cajero")
    public ResponseEntity<ReporteVentasDTO> obtenerReporteVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) MetodoPago metodoPago,
            @RequestParam(required = false) TipoComprobante tipoComprobante,
            @RequestParam(required = false) TipoEntrega tipoEntrega,
            @RequestParam(required = false) String cajero) {
        return ResponseEntity.ok(reporteService.obtenerReporteVentas(fechaInicio, fechaFin, metodoPago, tipoComprobante, tipoEntrega, cajero));
    }

    @GetMapping("/libro-iva")
    @Operation(summary = "Generar Libro de Ventas Fiscal con desglose de IVA (10%, 5% y Exentas) según régimen SET/DNIT")
    public ResponseEntity<LibroVentasIvaDTO> obtenerLibroVentasIva(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.obtenerLibroVentasIva(fechaInicio, fechaFin));
    }

    @GetMapping(value = "/libro-iva/csv", produces = "text/csv; charset=UTF-8")
    @Operation(summary = "Descargar Libro de Ventas Fiscal en formato CSV compatible con Excel y sistemas contables")
    public ResponseEntity<byte[]> descargarLibroVentasCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        byte[] csvData = reporteService.generarLibroVentasCsv(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"libro_ventas_iva.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/cajas")
    @Operation(summary = "Auditoría de turnos y arqueos de caja con cálculo de diferencias, sobrantes y faltantes")
    public ResponseEntity<ReporteCajasDTO> obtenerReporteCajas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String usuario) {
        return ResponseEntity.ok(reporteService.obtenerReporteCajas(fechaInicio, fechaFin, usuario));
    }

    @GetMapping("/ranking-productos")
    @Operation(summary = "Consultar ranking de demanda de presentaciones, productos simples, sabores y toppings")
    public ResponseEntity<RankingProductosDTO> obtenerRankingProductos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(reporteService.obtenerRankingProductos(fechaInicio, fechaFin, limite));
    }
}
