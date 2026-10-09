package com.heladeria.api.dto;

import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.enums.EstadoSesionCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SesionCajaDTO {

    private Long id;
    private EstadoSesionCaja estado;
    private String usuarioApertura;
    private String usuarioCierre;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoEsperadoEfectivo;
    private BigDecimal montoRealEfectivo;
    private BigDecimal diferencia;
    private BigDecimal totalVentasEfectivo;
    private BigDecimal totalVentasTarjetaDebito;
    private BigDecimal totalVentasTarjetaCredito;
    private BigDecimal totalVentasTransferenciaQr;
    private BigDecimal totalVentasGeneral;
    private Integer cantidadPedidos;
    private String observacionesApertura;
    private String observacionesCierre;

    public SesionCajaDTO() {
    }

    public SesionCajaDTO(Long id, EstadoSesionCaja estado, String usuarioApertura, String usuarioCierre,
                         LocalDateTime fechaApertura, LocalDateTime fechaCierre, BigDecimal montoInicial,
                         BigDecimal montoEsperadoEfectivo, BigDecimal montoRealEfectivo, BigDecimal diferencia,
                         BigDecimal totalVentasEfectivo, BigDecimal totalVentasTarjetaDebito,
                         BigDecimal totalVentasTarjetaCredito, BigDecimal totalVentasTransferenciaQr,
                         BigDecimal totalVentasGeneral, Integer cantidadPedidos,
                         String observacionesApertura, String observacionesCierre) {
        this.id = id;
        this.estado = estado;
        this.usuarioApertura = usuarioApertura;
        this.usuarioCierre = usuarioCierre;
        this.fechaApertura = fechaApertura;
        this.fechaCierre = fechaCierre;
        this.montoInicial = montoInicial;
        this.montoEsperadoEfectivo = montoEsperadoEfectivo;
        this.montoRealEfectivo = montoRealEfectivo;
        this.diferencia = diferencia;
        this.totalVentasEfectivo = totalVentasEfectivo;
        this.totalVentasTarjetaDebito = totalVentasTarjetaDebito;
        this.totalVentasTarjetaCredito = totalVentasTarjetaCredito;
        this.totalVentasTransferenciaQr = totalVentasTransferenciaQr;
        this.totalVentasGeneral = totalVentasGeneral;
        this.cantidadPedidos = cantidadPedidos;
        this.observacionesApertura = observacionesApertura;
        this.observacionesCierre = observacionesCierre;
    }

    public static SesionCajaDTO fromEntity(SesionCaja entity) {
        if (entity == null) {
            return null;
        }
        return new SesionCajaDTO(
                entity.getId(),
                entity.getEstado(),
                entity.getUsuarioApertura() != null ? entity.getUsuarioApertura().getUsername() : null,
                entity.getUsuarioCierre() != null ? entity.getUsuarioCierre().getUsername() : null,
                entity.getFechaApertura(),
                entity.getFechaCierre(),
                entity.getMontoInicial(),
                entity.getMontoEsperadoEfectivo(),
                entity.getMontoRealEfectivo(),
                entity.getDiferencia(),
                entity.getTotalVentasEfectivo(),
                entity.getTotalVentasTarjetaDebito(),
                entity.getTotalVentasTarjetaCredito(),
                entity.getTotalVentasTransferenciaQr(),
                entity.getTotalVentasGeneral(),
                entity.getCantidadPedidos(),
                entity.getObservacionesApertura(),
                entity.getObservacionesCierre()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EstadoSesionCaja getEstado() { return estado; }
    public void setEstado(EstadoSesionCaja estado) { this.estado = estado; }

    public String getUsuarioApertura() { return usuarioApertura; }
    public void setUsuarioApertura(String usuarioApertura) { this.usuarioApertura = usuarioApertura; }

    public String getUsuarioCierre() { return usuarioCierre; }
    public void setUsuarioCierre(String usuarioCierre) { this.usuarioCierre = usuarioCierre; }

    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }

    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }

    public BigDecimal getMontoInicial() { return montoInicial; }
    public void setMontoInicial(BigDecimal montoInicial) { this.montoInicial = montoInicial; }

    public BigDecimal getMontoEsperadoEfectivo() { return montoEsperadoEfectivo; }
    public void setMontoEsperadoEfectivo(BigDecimal montoEsperadoEfectivo) { this.montoEsperadoEfectivo = montoEsperadoEfectivo; }

    public BigDecimal getMontoRealEfectivo() { return montoRealEfectivo; }
    public void setMontoRealEfectivo(BigDecimal montoRealEfectivo) { this.montoRealEfectivo = montoRealEfectivo; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }

    public BigDecimal getTotalVentasEfectivo() { return totalVentasEfectivo; }
    public void setTotalVentasEfectivo(BigDecimal totalVentasEfectivo) { this.totalVentasEfectivo = totalVentasEfectivo; }

    public BigDecimal getTotalVentasTarjetaDebito() { return totalVentasTarjetaDebito; }
    public void setTotalVentasTarjetaDebito(BigDecimal totalVentasTarjetaDebito) { this.totalVentasTarjetaDebito = totalVentasTarjetaDebito; }

    public BigDecimal getTotalVentasTarjetaCredito() { return totalVentasTarjetaCredito; }
    public void setTotalVentasTarjetaCredito(BigDecimal totalVentasTarjetaCredito) { this.totalVentasTarjetaCredito = totalVentasTarjetaCredito; }

    public BigDecimal getTotalVentasTransferenciaQr() { return totalVentasTransferenciaQr; }
    public void setTotalVentasTransferenciaQr(BigDecimal totalVentasTransferenciaQr) { this.totalVentasTransferenciaQr = totalVentasTransferenciaQr; }

    public BigDecimal getTotalVentasGeneral() { return totalVentasGeneral; }
    public void setTotalVentasGeneral(BigDecimal totalVentasGeneral) { this.totalVentasGeneral = totalVentasGeneral; }

    public Integer getCantidadPedidos() { return cantidadPedidos; }
    public void setCantidadPedidos(Integer cantidadPedidos) { this.cantidadPedidos = cantidadPedidos; }

    public String getObservacionesApertura() { return observacionesApertura; }
    public void setObservacionesApertura(String observacionesApertura) { this.observacionesApertura = observacionesApertura; }

    public String getObservacionesCierre() { return observacionesCierre; }
    public void setObservacionesCierre(String observacionesCierre) { this.observacionesCierre = observacionesCierre; }
}
