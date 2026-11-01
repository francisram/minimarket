package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.CategoriaMovimientoCaja;
import com.heladeria.api.entities.enums.TipoMovimientoCaja;
import com.heladeria.api.util.MonedaPyUtils;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja", indexes = {
    @Index(name = "idx_mov_caja_sesion", columnList = "sesion_caja_id"),
    @Index(name = "idx_mov_caja_fecha", columnList = "fecha")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_caja_id", nullable = false)
    @JsonBackReference
    private SesionCaja sesionCaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimientoCaja tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaMovimientoCaja categoria;

    @Column(nullable = false, length = 200)
    private String concepto;

    @Column(length = 50)
    private String comprobante;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal monto;

    @Column(nullable = false, length = 50)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public MovimientoCaja() {
    }

    public MovimientoCaja(Long id, SesionCaja sesionCaja, TipoMovimientoCaja tipo,
                          CategoriaMovimientoCaja categoria, String concepto, String comprobante,
                          BigDecimal monto, String usuario, LocalDateTime fecha) {
        this.id = id;
        this.sesionCaja = sesionCaja;
        this.tipo = tipo;
        this.categoria = categoria;
        this.concepto = concepto;
        this.comprobante = comprobante;
        this.monto = MonedaPyUtils.redondearGs(monto);
        this.usuario = usuario;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.monto != null) {
            this.monto = MonedaPyUtils.redondearGs(this.monto);
        }
    }

    public static MovimientoCajaBuilder builder() {
        return new MovimientoCajaBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SesionCaja getSesionCaja() {
        return sesionCaja;
    }

    public void setSesionCaja(SesionCaja sesionCaja) {
        this.sesionCaja = sesionCaja;
    }

    public TipoMovimientoCaja getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimientoCaja tipo) {
        this.tipo = tipo;
    }

    public CategoriaMovimientoCaja getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMovimientoCaja categoria) {
        this.categoria = categoria;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = MonedaPyUtils.redondearGs(monto);
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public static class MovimientoCajaBuilder {
        private Long id;
        private SesionCaja sesionCaja;
        private TipoMovimientoCaja tipo;
        private CategoriaMovimientoCaja categoria;
        private String concepto;
        private String comprobante;
        private BigDecimal monto;
        private String usuario;
        private LocalDateTime fecha;

        public MovimientoCajaBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MovimientoCajaBuilder sesionCaja(SesionCaja sesionCaja) {
            this.sesionCaja = sesionCaja;
            return this;
        }

        public MovimientoCajaBuilder tipo(TipoMovimientoCaja tipo) {
            this.tipo = tipo;
            return this;
        }

        public MovimientoCajaBuilder categoria(CategoriaMovimientoCaja categoria) {
            this.categoria = categoria;
            return this;
        }

        public MovimientoCajaBuilder concepto(String concepto) {
            this.concepto = concepto;
            return this;
        }

        public MovimientoCajaBuilder comprobante(String comprobante) {
            this.comprobante = comprobante;
            return this;
        }

        public MovimientoCajaBuilder monto(BigDecimal monto) {
            this.monto = monto;
            return this;
        }

        public MovimientoCajaBuilder usuario(String usuario) {
            this.usuario = usuario;
            return this;
        }

        public MovimientoCajaBuilder fecha(LocalDateTime fecha) {
            this.fecha = fecha;
            return this;
        }

        public MovimientoCaja build() {
            return new MovimientoCaja(id, sesionCaja, tipo, categoria, concepto, comprobante, monto, usuario, fecha);
        }
    }
}
