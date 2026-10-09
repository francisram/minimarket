package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoEntrega;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PedidoRequestDTO {

    private String clienteNombre;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El tipo de entrega es obligatorio (MOSTRADOR, TAKE_AWAY, DELIVERY)")
    private TipoEntrega tipoEntrega;

    private String notas;

    @NotEmpty(message = "El pedido debe contener al menos un ítem")
    @Valid
    private List<DetallePedidoRequestDTO> items = new ArrayList<>();

    public PedidoRequestDTO() {
    }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public TipoEntrega getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(TipoEntrega tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public List<DetallePedidoRequestDTO> getItems() { return items; }
    public void setItems(List<DetallePedidoRequestDTO> items) { this.items = items; }
}
