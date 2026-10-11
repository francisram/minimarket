package com.heladeria.api.dto.reportes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RankingProductosDTO {
    private LocalDateTime periodoInicio;
    private LocalDateTime periodoFin;

    private List<ItemRankingDTO> topPresentaciones = new ArrayList<>();
    private List<ItemRankingDTO> topProductosSimples = new ArrayList<>();
    private List<SaborRankingDTO> topSabores = new ArrayList<>();
    private List<ItemRankingDTO> topToppings = new ArrayList<>();

    public RankingProductosDTO() {
    }

    public LocalDateTime getPeriodoInicio() {
        return periodoInicio;
    }

    public void setPeriodoInicio(LocalDateTime periodoInicio) {
        this.periodoInicio = periodoInicio;
    }

    public LocalDateTime getPeriodoFin() {
        return periodoFin;
    }

    public void setPeriodoFin(LocalDateTime periodoFin) {
        this.periodoFin = periodoFin;
    }

    public List<ItemRankingDTO> getTopPresentaciones() {
        return topPresentaciones;
    }

    public void setTopPresentaciones(List<ItemRankingDTO> topPresentaciones) {
        this.topPresentaciones = topPresentaciones;
    }

    public List<ItemRankingDTO> getTopProductosSimples() {
        return topProductosSimples;
    }

    public void setTopProductosSimples(List<ItemRankingDTO> topProductosSimples) {
        this.topProductosSimples = topProductosSimples;
    }

    public List<SaborRankingDTO> getTopSabores() {
        return topSabores;
    }

    public void setTopSabores(List<SaborRankingDTO> topSabores) {
        this.topSabores = topSabores;
    }

    public List<ItemRankingDTO> getTopToppings() {
        return topToppings;
    }

    public void setTopToppings(List<ItemRankingDTO> topToppings) {
        this.topToppings = topToppings;
    }
}
