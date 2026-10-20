package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "combo_componentes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_combo_componente", columnNames = {"combo_id", "componente_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ComboComponente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    @JsonBackReference
    private Producto combo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "componente_id", nullable = false)
    private Producto componente;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ONE;

    @Column(name = "notas", length = 100)
    private String notas;

    public ComboComponente() {
    }

    public ComboComponente(Producto combo, Producto componente, BigDecimal cantidad, String notas) {
        this.combo = combo;
        this.componente = componente;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.notas = notas;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getCombo() { return combo; }
    public void setCombo(Producto combo) { this.combo = combo; }

    public Producto getComponente() { return componente; }
    public void setComponente(Producto componente) { this.componente = componente; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}
