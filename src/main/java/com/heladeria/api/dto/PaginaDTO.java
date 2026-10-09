package com.heladeria.api.dto;

public class PaginaDTO {

    private Long id;
    private String clave;
    private String nombre;
    private String url;
    private Long idPadre;
    private String icono;
    private Integer orden;

    public PaginaDTO() {
    }

    public PaginaDTO(Long id, String clave, String nombre, String url, Long idPadre, String icono, Integer orden) {
        this.id = id;
        this.clave = clave;
        this.nombre = nombre;
        this.url = url;
        this.idPadre = idPadre;
        this.icono = icono;
        this.orden = orden;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Long getIdPadre() { return idPadre; }
    public void setIdPadre(Long idPadre) { this.idPadre = idPadre; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
