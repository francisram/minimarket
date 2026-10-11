package com.heladeria.api.dto;

import com.heladeria.api.entities.Institucion;
import java.time.LocalDate;

public class InstitucionDTO {

    private String nombre;
    private String logoBase64;
    private String ruc;
    private String timbrado;
    private LocalDate timbradoVencimiento;
    private String establecimiento;
    private String puntoEmision;
    private Long ultimoNumeroFactura;
    private String direccion;
    private String telefono;
    private String ciudad;
    private LocalDate timbradoInicio;
    private Long numeroInicioFactura;
    private Long numeroFinFactura;

    public InstitucionDTO() {
    }

    public InstitucionDTO(String nombre, String logoBase64) {
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
    }

    public InstitucionDTO(String nombre, String logoBase64, String ruc, String timbrado,
                          LocalDate timbradoVencimiento, String establecimiento, String puntoEmision,
                          Long ultimoNumeroFactura, String direccion, String telefono, String ciudad) {
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
        this.ruc = ruc;
        this.timbrado = timbrado;
        this.timbradoVencimiento = timbradoVencimiento;
        this.establecimiento = establecimiento;
        this.puntoEmision = puntoEmision;
        this.ultimoNumeroFactura = ultimoNumeroFactura;
        this.direccion = direccion;
        this.telefono = telefono;
        this.ciudad = ciudad;
    }

    public InstitucionDTO(String nombre, String logoBase64, String ruc, String timbrado,
                          LocalDate timbradoVencimiento, LocalDate timbradoInicio,
                          Long numeroInicioFactura, Long numeroFinFactura,
                          String establecimiento, String puntoEmision,
                          Long ultimoNumeroFactura, String direccion, String telefono, String ciudad) {
        this(nombre, logoBase64, ruc, timbrado, timbradoVencimiento, establecimiento, puntoEmision,
                ultimoNumeroFactura, direccion, telefono, ciudad);
        this.timbradoInicio = timbradoInicio;
        this.numeroInicioFactura = numeroInicioFactura;
        this.numeroFinFactura = numeroFinFactura;
    }

    public static InstitucionDTO from(Institucion inst) {
        if (inst == null) {
            return null;
        }
        InstitucionDTO dto = new InstitucionDTO(
                inst.getNombre(),
                inst.getLogoBase64(),
                inst.getRuc(),
                inst.getTimbrado(),
                inst.getTimbradoVencimiento(),
                inst.getEstablecimiento(),
                inst.getPuntoEmision(),
                inst.getUltimoNumeroFactura(),
                inst.getDireccion(),
                inst.getTelefono(),
                inst.getCiudad()
        );
        dto.setTimbradoInicio(inst.getTimbradoInicio());
        dto.setNumeroInicioFactura(inst.getNumeroInicioFactura());
        dto.setNumeroFinFactura(inst.getNumeroFinFactura());
        return dto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLogoBase64() {
        return logoBase64;
    }

    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getTimbrado() {
        return timbrado;
    }

    public void setTimbrado(String timbrado) {
        this.timbrado = timbrado;
    }

    public LocalDate getTimbradoVencimiento() {
        return timbradoVencimiento;
    }

    public void setTimbradoVencimiento(LocalDate timbradoVencimiento) {
        this.timbradoVencimiento = timbradoVencimiento;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoEmision() {
        return puntoEmision;
    }

    public void setPuntoEmision(String puntoEmision) {
        this.puntoEmision = puntoEmision;
    }

    public Long getUltimoNumeroFactura() {
        return ultimoNumeroFactura;
    }

    public void setUltimoNumeroFactura(Long ultimoNumeroFactura) {
        this.ultimoNumeroFactura = ultimoNumeroFactura;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public LocalDate getTimbradoInicio() {
        return timbradoInicio;
    }

    public void setTimbradoInicio(LocalDate timbradoInicio) {
        this.timbradoInicio = timbradoInicio;
    }

    public Long getNumeroInicioFactura() {
        return numeroInicioFactura;
    }

    public void setNumeroInicioFactura(Long numeroInicioFactura) {
        this.numeroInicioFactura = numeroInicioFactura;
    }

    public Long getNumeroFinFactura() {
        return numeroFinFactura;
    }

    public void setNumeroFinFactura(Long numeroFinFactura) {
        this.numeroFinFactura = numeroFinFactura;
    }
}
