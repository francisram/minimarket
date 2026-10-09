package com.heladeria.api.dto.stock;

public class AlertaStockItemDTO {

    private Long id;
    private String nombre;
    private String tipo; // "PRODUCTO", "SABOR", "ENVASE"
    private Double stockActual;
    private Double stockMinimo;
    private String unidad; // "UNIDADES", "KG"
    private String estadoAlerta; // "AGOTADO", "CRITICO", "BAJO"
    private Boolean disponible;

    public AlertaStockItemDTO() {
    }

    public AlertaStockItemDTO(Long id, String nombre, String tipo, Double stockActual, Double stockMinimo,
                              String unidad, String estadoAlerta, Boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.unidad = unidad;
        this.estadoAlerta = estadoAlerta;
        this.disponible = disponible;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getStockActual() { return stockActual; }
    public void setStockActual(Double stockActual) { this.stockActual = stockActual; }

    public Double getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Double stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getEstadoAlerta() { return estadoAlerta; }
    public void setEstadoAlerta(String estadoAlerta) { this.estadoAlerta = estadoAlerta; }

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}
