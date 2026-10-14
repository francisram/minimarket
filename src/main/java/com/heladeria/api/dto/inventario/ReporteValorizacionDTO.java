package com.heladeria.api.dto.inventario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReporteValorizacionDTO {

    private BigDecimal totalValorizadoGeneral = BigDecimal.ZERO;
    private int totalArticulos = 0;
    private int totalAgotados = 0;
    private int totalBajoMinimo = 0;
    private List<ValorizacionItemDTO> items = new ArrayList<>();

    public ReporteValorizacionDTO() {}

    public ReporteValorizacionDTO(BigDecimal totalValorizadoGeneral, int totalArticulos, int totalAgotados, int totalBajoMinimo, List<ValorizacionItemDTO> items) {
        this.totalValorizadoGeneral = totalValorizadoGeneral;
        this.totalArticulos = totalArticulos;
        this.totalAgotados = totalAgotados;
        this.totalBajoMinimo = totalBajoMinimo;
        this.items = items;
    }

    public BigDecimal getTotalValorizadoGeneral() { return totalValorizadoGeneral; }
    public void setTotalValorizadoGeneral(BigDecimal totalValorizadoGeneral) { this.totalValorizadoGeneral = totalValorizadoGeneral; }

    public int getTotalArticulos() { return totalArticulos; }
    public void setTotalArticulos(int totalArticulos) { this.totalArticulos = totalArticulos; }

    public int getTotalAgotados() { return totalAgotados; }
    public void setTotalAgotados(int totalAgotados) { this.totalAgotados = totalAgotados; }

    public int getTotalBajoMinimo() { return totalBajoMinimo; }
    public void setTotalBajoMinimo(int totalBajoMinimo) { this.totalBajoMinimo = totalBajoMinimo; }

    public List<ValorizacionItemDTO> getItems() { return items; }
    public void setItems(List<ValorizacionItemDTO> items) { this.items = items; }
}
