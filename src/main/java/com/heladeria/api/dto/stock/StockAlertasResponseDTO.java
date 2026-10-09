package com.heladeria.api.dto.stock;

import java.util.ArrayList;
import java.util.List;

public class StockAlertasResponseDTO {

    private List<AlertaStockItemDTO> productosBajoStock = new ArrayList<>();
    private List<AlertaStockItemDTO> saboresBajoStock = new ArrayList<>();
    private List<AlertaStockItemDTO> presentacionesBajoStock = new ArrayList<>();
    private int totalAlertas;
    private int totalAgotados;
    private int totalBajoStock;

    public StockAlertasResponseDTO() {
    }

    public List<AlertaStockItemDTO> getProductosBajoStock() { return productosBajoStock; }
    public void setProductosBajoStock(List<AlertaStockItemDTO> productosBajoStock) { this.productosBajoStock = productosBajoStock; }

    public List<AlertaStockItemDTO> getSaboresBajoStock() { return saboresBajoStock; }
    public void setSaboresBajoStock(List<AlertaStockItemDTO> saboresBajoStock) { this.saboresBajoStock = saboresBajoStock; }

    public List<AlertaStockItemDTO> getPresentacionesBajoStock() { return presentacionesBajoStock; }
    public void setPresentacionesBajoStock(List<AlertaStockItemDTO> presentacionesBajoStock) { this.presentacionesBajoStock = presentacionesBajoStock; }

    public int getTotalAlertas() { return totalAlertas; }
    public void setTotalAlertas(int totalAlertas) { this.totalAlertas = totalAlertas; }

    public int getTotalAgotados() { return totalAgotados; }
    public void setTotalAgotados(int totalAgotados) { this.totalAgotados = totalAgotados; }

    public int getTotalBajoStock() { return totalBajoStock; }
    public void setTotalBajoStock(int totalBajoStock) { this.totalBajoStock = totalBajoStock; }
}
