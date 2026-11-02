package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Representa un Punto de Expedición / Terminal de Caja habilitado por la DNIT (ex SET)
 * para la emisión de comprobantes de venta bajo el formato legal:
 * Establecimiento (3 dígitos) - Punto de Emisión (3 dígitos) - Secuencial (7 dígitos).
 */
@Entity
@Table(name = "puntos_expedicion", uniqueConstraints = {
        @UniqueConstraint(name = "uk_punto_expedicion_estab_punto", columnNames = {"establecimiento", "punto_emision"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PuntoExpedicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "establecimiento", nullable = false, length = 3)
    private String establecimiento = "001";

    @Column(name = "punto_emision", nullable = false, length = 3)
    private String puntoEmision = "001";

    @Column(length = 20)
    private String timbrado;

    @Column
    private LocalDate timbradoInicio;

    @Column
    private LocalDate timbradoVencimiento;

    @Column(name = "numero_inicio_factura", nullable = false)
    private Long numeroInicioFactura = 1L;

    @Column(name = "numero_fin_factura", nullable = false)
    private Long numeroFinFactura = 1000L;

    @Column(name = "ultimo_numero_factura", nullable = false)
    private Long ultimoNumeroFactura = 0L;

    @Column(nullable = false)
    private Boolean activo = true;

    public PuntoExpedicion() {
    }

    public PuntoExpedicion(Long id, String nombre, String establecimiento, String puntoEmision,
                           String timbrado, LocalDate timbradoInicio, LocalDate timbradoVencimiento,
                           Long numeroInicioFactura, Long numeroFinFactura, Long ultimoNumeroFactura,
                           Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.establecimiento = establecimiento != null ? establecimiento : "001";
        this.puntoEmision = puntoEmision != null ? puntoEmision : "001";
        this.timbrado = timbrado;
        this.timbradoInicio = timbradoInicio;
        this.timbradoVencimiento = timbradoVencimiento;
        this.numeroInicioFactura = numeroInicioFactura != null ? numeroInicioFactura : 1L;
        this.numeroFinFactura = numeroFinFactura != null ? numeroFinFactura : 1000L;
        this.ultimoNumeroFactura = ultimoNumeroFactura != null ? ultimoNumeroFactura : 0L;
        this.activo = activo != null ? activo : true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEstablecimiento() { return establecimiento; }
    public void setEstablecimiento(String establecimiento) { this.establecimiento = establecimiento; }

    public String getPuntoEmision() { return puntoEmision; }
    public void setPuntoEmision(String puntoEmision) { this.puntoEmision = puntoEmision; }

    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }

    public LocalDate getTimbradoInicio() { return timbradoInicio; }
    public void setTimbradoInicio(LocalDate timbradoInicio) { this.timbradoInicio = timbradoInicio; }

    public LocalDate getTimbradoVencimiento() { return timbradoVencimiento; }
    public void setTimbradoVencimiento(LocalDate timbradoVencimiento) { this.timbradoVencimiento = timbradoVencimiento; }

    public Long getNumeroInicioFactura() { return numeroInicioFactura; }
    public void setNumeroInicioFactura(Long numeroInicioFactura) { this.numeroInicioFactura = numeroInicioFactura; }

    public Long getNumeroFinFactura() { return numeroFinFactura; }
    public void setNumeroFinFactura(Long numeroFinFactura) { this.numeroFinFactura = numeroFinFactura; }

    public Long getUltimoNumeroFactura() { return ultimoNumeroFactura; }
    public void setUltimoNumeroFactura(Long ultimoNumeroFactura) { this.ultimoNumeroFactura = ultimoNumeroFactura; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    /**
     * Retorna el código de prefijo legal en formato "EEE-PPP" (ej. "001-001").
     */
    public String getPrefijoLegal() {
        String est = (establecimiento != null && !establecimiento.isBlank()) ? establecimiento : "001";
        String pto = (puntoEmision != null && !puntoEmision.isBlank()) ? puntoEmision : "001";
        return String.format("%03d-%03d", Integer.parseInt(est.replaceAll("\\D", "1")), Integer.parseInt(pto.replaceAll("\\D", "1")));
    }

    /**
     * Retorna la representación formateada del siguiente número de factura (ej. "001-001-0000005").
     */
    public String getProximaFacturaFormateada() {
        long sig = (ultimoNumeroFactura != null ? ultimoNumeroFactura : 0L) + 1;
        String est = (establecimiento != null && !establecimiento.isBlank()) ? establecimiento : "001";
        String pto = (puntoEmision != null && !puntoEmision.isBlank()) ? puntoEmision : "001";
        return String.format("%s-%s-%07d", est, pto, sig);
    }

    public static class Builder {
        private Long id;
        private String nombre;
        private String establecimiento = "001";
        private String puntoEmision = "001";
        private String timbrado;
        private LocalDate timbradoInicio;
        private LocalDate timbradoVencimiento;
        private Long numeroInicioFactura = 1L;
        private Long numeroFinFactura = 1000L;
        private Long ultimoNumeroFactura = 0L;
        private Boolean activo = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder establecimiento(String establecimiento) { this.establecimiento = establecimiento; return this; }
        public Builder puntoEmision(String puntoEmision) { this.puntoEmision = puntoEmision; return this; }
        public Builder timbrado(String timbrado) { this.timbrado = timbrado; return this; }
        public Builder timbradoInicio(LocalDate timbradoInicio) { this.timbradoInicio = timbradoInicio; return this; }
        public Builder timbradoVencimiento(LocalDate timbradoVencimiento) { this.timbradoVencimiento = timbradoVencimiento; return this; }
        public Builder numeroInicioFactura(Long numeroInicioFactura) { this.numeroInicioFactura = numeroInicioFactura; return this; }
        public Builder numeroFinFactura(Long numeroFinFactura) { this.numeroFinFactura = numeroFinFactura; return this; }
        public Builder ultimoNumeroFactura(Long ultimoNumeroFactura) { this.ultimoNumeroFactura = ultimoNumeroFactura; return this; }
        public Builder activo(Boolean activo) { this.activo = activo; return this; }

        public PuntoExpedicion build() {
            return new PuntoExpedicion(id, nombre, establecimiento, puntoEmision, timbrado, timbradoInicio,
                    timbradoVencimiento, numeroInicioFactura, numeroFinFactura, ultimoNumeroFactura, activo);
        }
    }
}
