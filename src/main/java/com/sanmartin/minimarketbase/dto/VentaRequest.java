package com.sanmartin.minimarketbase.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class VentaRequest {
    private Integer idCliente;
    private String metodoPago;
    private Integer idUsuario; // Para auditoría
    private List<DetalleVentaRequest> detalles;
    
    @Data
    public static class DetalleVentaRequest {
        private Integer idProducto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}