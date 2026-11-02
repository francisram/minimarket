package com.heladeria.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de entrada para dar de alta o modificar un Punto de Expedición / Terminal de Venta.
 */
public class PuntoExpedicionRequestDTO {

    @NotBlank(message = "El nombre de la caja o terminal es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String nombre;

    @NotBlank(message = "El establecimiento es obligatorio (ej. 001)")
    @Pattern(regexp = "^[0-9]{3}$", message = "El código de establecimiento debe tener 3 dígitos (ej. 001)")
    private String establecimiento = "001";

    @NotBlank(message = "El punto de emisión es obligatorio (ej. 001)")
    @Pattern(regexp = "^[0-9]{3}$", message = "El código de punto de emisión debe tener 3 dígitos (ej. 001)")
    private String puntoEmision = "001";

    @Size(max = 20, message = "El timbrado no puede superar 20 caracteres")
    private String timbrado;

    private LocalDate timbradoInicio;

    private LocalDate timbradoVencimiento;

    @Min(value = 1, message = "El número de inicio del talonario debe ser al menos 1")
    private Long numeroInicioFactura = 1L;

    @Min(value = 1, message = "El número final del talonario debe ser al menos 1")
    private Long numeroFinFactura = 1000L;

    @Min(value = 0, message = "El último número emitido no puede ser negativo")
    private Long ultimoNumeroFactura = 0L;

    private Boolean activo = true;

    public PuntoExpedicionRequestDTO() {
    }

    public PuntoExpedicionRequestDTO(String nombre, String establecimiento, String puntoEmision,
                                     String timbrado, LocalDate timbradoInicio, LocalDate timbradoVencimiento,
                                     Long numeroInicioFactura, Long numeroFinFactura, Long ultimoNumeroFactura,
                                     Boolean activo) {
        this.nombre = nombre;
        this.establecimiento = establecimiento;
        this.puntoEmision = puntoEmision;
        this.timbrado = timbrado;
        this.timbradoInicio = timbradoInicio;
        this.timbradoVencimiento = timbradoVencimiento;
        this.numeroInicioFactura = numeroInicioFactura;
        this.numeroFinFactura = numeroFinFactura;
        this.ultimoNumeroFactura = ultimoNumeroFactura;
        this.activo = activo != null ? activo : true;
    }

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
}
