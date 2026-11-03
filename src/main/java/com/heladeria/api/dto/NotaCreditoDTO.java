package com.heladeria.api.dto;

import com.heladeria.api.entities.DetalleNotaCredito;
import com.heladeria.api.entities.NotaCredito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class NotaCreditoDTO {

    public record Linea(Long detalleId, String descripcion, Integer cantidad, BigDecimal precioUnitario,
                        BigDecimal subtotal, String tipoIva) { }

    public Long id;
    public String numero;
    public String timbrado;
    public LocalDate timbradoVencimiento;
    public LocalDateTime fecha;
    public String motivo;
    public boolean reintegraStock;
    public String usuario;
    public String autorizadoPor;
    public Long pedidoId;
    public String facturaNumero;
    public String facturaTimbrado;
    public String clienteNombre;
    public String clienteRuc;
    public BigDecimal totalExentas, totalGravada5, totalGravada10, totalIva5, totalIva10, totalIva, total;
    public List<Linea> lineas;
    /** Si se devolvió el dinero en efectivo desde la caja del usuario (solo ventas en efectivo con caja abierta). */
    public boolean egresoCajaRegistrado;

    public static NotaCreditoDTO desde(NotaCredito n) {
        NotaCreditoDTO d = new NotaCreditoDTO();
        d.id = n.getId();
        d.numero = n.getNumero();
        d.timbrado = n.getTimbrado();
        d.timbradoVencimiento = n.getTimbradoVencimiento();
        d.fecha = n.getFecha();
        d.motivo = n.getMotivo();
        d.reintegraStock = n.isReintegraStock();
        d.usuario = n.getUsuario();
        d.autorizadoPor = n.getAutorizadoPor();
        d.pedidoId = n.getPedido().getId();
        d.facturaNumero = n.getPedido().getNumeroFactura();
        d.facturaTimbrado = n.getPedido().getTimbrado();
        d.clienteNombre = n.getPedido().getClienteNombre();
        d.clienteRuc = n.getPedido().getClienteRuc();
        d.totalExentas = n.getTotalExentas();
        d.totalGravada5 = n.getTotalGravada5();
        d.totalGravada10 = n.getTotalGravada10();
        d.totalIva5 = n.getTotalIva5();
        d.totalIva10 = n.getTotalIva10();
        d.totalIva = n.getTotalIva();
        d.total = n.getTotal();
        d.lineas = n.getDetalles().stream().map((DetalleNotaCredito x) ->
                new Linea(x.getDetallePedido().getId(), x.getDescripcion(), x.getCantidad(), x.getPrecioUnitario(),
                        x.getSubtotal(), x.getTipoIva().name())).toList();
        return d;
    }
}
