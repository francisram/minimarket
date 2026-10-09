package com.heladeria.api.dto;

import com.heladeria.api.entities.Institucion;

public class InstitucionDTO {

    private String nombre;
    private String logoBase64;

    public InstitucionDTO() {
    }

    public InstitucionDTO(String nombre, String logoBase64) {
        this.nombre = nombre;
        this.logoBase64 = logoBase64;
    }

    public static InstitucionDTO from(Institucion inst) {
        if (inst == null) {
            return null;
        }
        return new InstitucionDTO(inst.getNombre(), inst.getLogoBase64());
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
}
