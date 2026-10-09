package com.heladeria.api.entities;

import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.util.MonedaPyUtils;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesiones_caja")
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_apertura_id", nullable = false)
    private Usuario usuarioApertura;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    @Column
    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal montoInicial;

    @Column(precision = 12, scale = 0)
    private BigDecimal montoEsperadoEfectivo;

    @Column(precision = 12, scale = 0)
    private BigDecimal montoRealEfectivo;

    @Column(precision = 12, scale = 0)
    private BigDecimal diferencia;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalVentasEfectivo;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalVentasTarjetaDebito;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalVentasTarjetaCredito;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalVentasTransferenciaQr;

    @Column(precision = 12, scale = 0)
    private BigDecimal totalVentasGeneral;

    @Column
    private Integer cantidadPedidos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesionCaja estado;

    @Column(length = 255)
    private String observacionesApertura;

    @Column(length = 255)
    private String observacionesCierre;

    public SesionCaja() {
    }

    public SesionCaja(Long id, Usuario usuarioApertura, Usuario usuarioCierre, LocalDateTime fechaApertura,
                      LocalDateTime fechaCierre, BigDecimal montoInicial, BigDecimal montoEsperadoEfectivo,
                      BigDecimal montoRealEfectivo, BigDecimal diferencia, BigDecimal totalVentasEfectivo,
                      BigDecimal totalVentasTarjetaDebito, BigDecimal totalVentasTarjetaCredito,
                      BigDecimal totalVentasTransferenciaQr, BigDecimal totalVentasGeneral, Integer cantidadPedidos,
                      EstadoSesionCaja estado, String observacionesApertura, String observacionesCierre) {
        this.id = id;
        this.usuarioApertura = usuarioApertura;
        this.usuarioCierre = usuarioCierre;
        this.fechaApertura = fechaApertura != null ? fechaApertura : LocalDateTime.now();
        this.fechaCierre = fechaCierre;
        this.montoInicial = MonedaPyUtils.redondearGs(montoInicial);
        this.montoEsperadoEfectivo = montoEsperadoEfectivo != null ? MonedaPyUtils.redondearGs(montoEsperadoEfectivo) : null;
        this.montoRealEfectivo = montoRealEfectivo != null ? MonedaPyUtils.redondearGs(montoRealEfectivo) : null;
        this.diferencia = diferencia != null ? MonedaPyUtils.redondearGs(diferencia) : null;
        this.totalVentasEfectivo = totalVentasEfectivo != null ? MonedaPyUtils.redondearGs(totalVentasEfectivo) : BigDecimal.ZERO;
        this.totalVentasTarjetaDebito = totalVentasTarjetaDebito != null ? MonedaPyUtils.redondearGs(totalVentasTarjetaDebito) : BigDecimal.ZERO;
        this.totalVentasTarjetaCredito = totalVentasTarjetaCredito != null ? MonedaPyUtils.redondearGs(totalVentasTarjetaCredito) : BigDecimal.ZERO;
        this.totalVentasTransferenciaQr = totalVentasTransferenciaQr != null ? MonedaPyUtils.redondearGs(totalVentasTransferenciaQr) : BigDecimal.ZERO;
        this.totalVentasGeneral = totalVentasGeneral != null ? MonedaPyUtils.redondearGs(totalVentasGeneral) : BigDecimal.ZERO;
        this.cantidadPedidos = cantidadPedidos != null ? cantidadPedidos : 0;
        this.estado = estado != null ? estado : EstadoSesionCaja.ABIERTA;
        this.observacionesApertura = observacionesApertura;
        this.observacionesCierre = observacionesCierre;
    }

    public static SesionCajaBuilder builder() {
        return new SesionCajaBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaApertura == null) {
            this.fechaApertura = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoSesionCaja.ABIERTA;
        }
        if (this.montoInicial == null) {
            this.montoInicial = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuarioApertura() { return usuarioApertura; }
    public void setUsuarioApertura(Usuario usuarioApertura) { this.usuarioApertura = usuarioApertura; }

    public Usuario getUsuarioCierre() { return usuarioCierre; }
    public void setUsuarioCierre(Usuario usuarioCierre) { this.usuarioCierre = usuarioCierre; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = MonedaPyUtils.redondearGs(montoInicial);
    }

    public BigDecimal getMontoEsperadoEfectivo() { return montoEsperadoEfectivo; }
    public void setMontoEsperadoEfectivo(BigDecimal montoEsperadoEfectivo) {
        this.montoEsperadoEfectivo = montoEsperadoEfectivo != null ? MonedaPyUtils.redondearGs(montoEsperadoEfectivo) : null;
    }

    public BigDecimal getMontoRealEfectivo() { return montoRealEfectivo; }
    public void setMontoRealEfectivo(BigDecimal montoRealEfectivo) {
        this.montoRealEfectivo = montoRealEfectivo != null ? MonedaPyUtils.redondearGs(montoRealEfectivo) : null;
    }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia != null ? MonedaPyUtils.redondearGs(diferencia) : null;
    }

    public BigDecimal getTotalVentasEfectivo() { return totalVentasEfectivo; }
    public void setTotalVentasEfectivo(BigDecimal totalVentasEfectivo) {
        this.totalVentasEfectivo = totalVentasEfectivo != null ? MonedaPyUtils.redondearGs(totalVentasEfectivo) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalVentasTarjetaDebito() { return totalVentasTarjetaDebito; }
    public void setTotalVentasTarjetaDebito(BigDecimal totalVentasTarjetaDebito) {
        this.totalVentasTarjetaDebito = totalVentasTarjetaDebito != null ? MonedaPyUtils.redondearGs(totalVentasTarjetaDebito) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalVentasTarjetaCredito() { return totalVentasTarjetaCredito; }
    public void setTotalVentasTarjetaCredito(BigDecimal totalVentasTarjetaCredito) {
        this.totalVentasTarjetaCredito = totalVentasTarjetaCredito != null ? MonedaPyUtils.redondearGs(totalVentasTarjetaCredito) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalVentasTransferenciaQr() { return totalVentasTransferenciaQr; }
    public void setTotalVentasTransferenciaQr(BigDecimal totalVentasTransferenciaQr) {
        this.totalVentasTransferenciaQr = totalVentasTransferenciaQr != null ? MonedaPyUtils.redondearGs(totalVentasTransferenciaQr) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalVentasGeneral() { return totalVentasGeneral; }
    public void setTotalVentasGeneral(BigDecimal totalVentasGeneral) {
        this.totalVentasGeneral = totalVentasGeneral != null ? MonedaPyUtils.redondearGs(totalVentasGeneral) : BigDecimal.ZERO;
    }

    public Integer getCantidadPedidos() { return cantidadPedidos; }
    public void setCantidadPedidos(Integer cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; }

    public EstadoSesionCaja getEstado() { return estado; }
    public void setEstado(EstadoSesionCaja estado) { this.estado = estado; }

    public String getObservacionesApertura() { return observacionesApertura; }
    public void setObservacionesApertura(String observacionesApertura) { this.observacionesApertura = observacionesApertura; }

    public String getObservacionesCierre() { return observacionesCierre; }
    public void setObservacionesCierre(String observacionesCierre) { this.observacionesCierre = observacionesCierre; }

    public static class SesionCajaBuilder {
        private Long id;
        private Usuario usuarioApertura;
        private Usuario usuarioCierre;
        private LocalDateTime fechaApertura;
        private LocalDateTime fechaCierre;
        private BigDecimal montoInicial = BigDecimal.ZERO;
        private BigDecimal montoEsperadoEfectivo;
        private BigDecimal montoRealEfectivo;
        private BigDecimal diferencia;
        private BigDecimal totalVentasEfectivo = BigDecimal.ZERO;
        private BigDecimal totalVentasTarjetaDebito = BigDecimal.ZERO;
        private BigDecimal totalVentasTarjetaCredito = BigDecimal.ZERO;
        private BigDecimal totalVentasTransferenciaQr = BigDecimal.ZERO;
        private BigDecimal totalVentasGeneral = BigDecimal.ZERO;
        private Integer cantidadPedidos = 0;
        private EstadoSesionCaja estado = EstadoSesionCaja.ABIERTA;
        private String observacionesApertura;
        private String observacionesCierre;

        public SesionCajaBuilder id(Long id) { this.id = id; return this; }
        public SesionCajaBuilder usuarioApertura(Usuario usuarioApertura) { this.usuarioApertura = usuarioApertura; return this; }
        public SesionCajaBuilder usuarioCierre(Usuario usuarioCierre) { this.usuarioCierre = usuarioCierre; return this; }
        public SesionCajaBuilder fechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; return this; }
        public SesionCajaBuilder fechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; return this; }
        public SesionCajaBuilder montoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; return this; }
        public SesionCajaBuilder montoEsperadoEfectivo(BigDecimal montoEsperadoEfectivo) { this.montoEsperadoEfectivo = montoEsperadoEfectivo; return this; }
        public SesionCajaBuilder montoRealEfectivo(BigDecimal montoRealEfectivo) { this.montoRealEfectivo = montoRealEfectivo; return this; }
        public SesionCajaBuilder diferencia(BigDecimal diferencia) { this.diferencia = diferencia; return this; }
        public SesionCajaBuilder totalVentasEfectivo(BigDecimal totalVentasEfectivo) { this.totalVentasEfectivo = totalVentasEfectivo; return this; }
        public SesionCajaBuilder totalVentasTarjetaDebito(BigDecimal totalVentasTarjetaDebito) { this.totalVentasTarjetaDebito = totalVentasTarjetaDebito; return this; }
        public SesionCajaBuilder totalVentasTarjetaCredito(BigDecimal totalVentasTarjetaCredito) { this.totalVentasTarjetaCredito = totalVentasTarjetaCredito; return this; }
        public SesionCajaBuilder totalVentasTransferenciaQr(BigDecimal totalVentasTransferenciaQr) { this.totalVentasTransferenciaQr = totalVentasTransferenciaQr; return this; }
        public SesionCajaBuilder totalVentasGeneral(BigDecimal totalVentasGeneral) { this.totalVentasGeneral = totalVentasGeneral; return this; }
        public SesionCajaBuilder cantidadPedidos(Integer cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; return this; }
        public SesionCajaBuilder estado(EstadoSesionCaja estado) { this.estado = estado; return this; }
        public SesionCajaBuilder observacionesApertura(String observacionesApertura) { this.observacionesApertura = observacionesApertura; return this; }
        public SesionCajaBuilder observacionesCierre(String observacionesCierre) { this.observacionesCierre = observacionesCierre; return this; }

        public SesionCaja build() {
            return new SesionCaja(id, usuarioApertura, usuarioCierre, fechaApertura, fechaCierre, montoInicial,
                    montoEsperadoEfectivo, montoRealEfectivo, diferencia, totalVentasEfectivo,
                    totalVentasTarjetaDebito, totalVentasTarjetaCredito, totalVentasTransferenciaQr,
                    totalVentasGeneral, cantidadPedidos, estado, observacionesApertura, observacionesCierre);
        }
    }
}
