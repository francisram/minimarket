package com.heladeria.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Una factura con lo que todavía se puede acreditar de cada línea. */
public class FacturaParaNotaCreditoDTO {

    public record Linea(Long detalleId, String descripcion, int cantidad, long cantidadAcreditada, long cantidadDisponible,
                        BigDecimal precioUnitario, BigDecimal subtotal, String tipoIva) { }

    public Long pedidoId;
    public String numeroFactura;
    public String timbrado;
    public LocalDateTime fecha;
    public String clienteNombre;
    public String clienteRuc;
    public String metodoPago;
    public String estado;
    public BigDecimal total;
    public Long puntoExpedicionId;
    public boolean serieConfigurada;
    public LocalDate serieVencimiento;
    public List<Linea> lineas;
    public List<NotaCreditoDTO> notasCredito;
}
