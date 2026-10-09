package com.heladeria.api.dto;

import com.heladeria.api.entities.enums.TipoItemPedido;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DetallePedidoRequestDTO {

    @NotNull(message = "El tipo de ítem es obligatorio (HELADO o PRODUCTO_SIMPLE)")
    private TipoItemPedido tipoItem;

    private Long presentacionId;
    private List<Long> saborIds = new ArrayList<>();
    private List<Long> toppingIds = new ArrayList<>();

    private Long productoSimpleId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad = 1;

    public DetallePedidoRequestDTO() {
    }

    public TipoItemPedido getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemPedido tipoItem) { this.tipoItem = tipoItem; }

    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }

    public List<Long> getSaborIds() { return saborIds; }
    public void setSaborIds(List<Long> saborIds) { this.saborIds = saborIds; }

    public List<Long> getToppingIds() { return toppingIds; }
    public void setToppingIds(List<Long> toppingIds) { this.toppingIds = toppingIds; }

    public Long getProductoSimpleId() { return productoSimpleId; }
    public void setProductoSimpleId(Long productoSimpleId) { this.productoSimpleId = productoSimpleId; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
