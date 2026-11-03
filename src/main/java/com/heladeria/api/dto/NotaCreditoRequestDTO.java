package com.heladeria.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class NotaCreditoRequestDTO {

    public static class Linea {
        @NotNull
        private Long detalleId;
        @NotNull
        @Min(1)
        private Integer cantidad;

        public Long getDetalleId() { return detalleId; }
        public void setDetalleId(Long detalleId) { this.detalleId = detalleId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }

    @NotBlank(message = "El motivo de la nota de crédito es obligatorio")
    @Size(max = 300)
    private String motivo;

    /** Líneas a acreditar; vacío = toda la factura. */
    @Valid
    private List<Linea> items = new ArrayList<>();

    /** Si las mercaderías vuelven al inventario (devolución física). */
    private boolean reintegrarStock = true;

    /** Clave de un administrador, cuando quien emite no es administrador ni supervisor. */
    private String adminPassword;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public List<Linea> getItems() { return items; }
    public void setItems(List<Linea> items) { this.items = items; }
    public boolean isReintegrarStock() { return reintegrarStock; }
    public void setReintegrarStock(boolean reintegrarStock) { this.reintegrarStock = reintegrarStock; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
