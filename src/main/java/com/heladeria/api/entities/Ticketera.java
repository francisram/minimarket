package com.heladeria.api.entities;

import jakarta.persistence.*;

/**
 * Kiosco o punto de emisión de tickets que referencia una impresora registrada.
 */
@Entity
@Table(name = "ticketeras")
public class Ticketera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "impresora_id", nullable = false)
    private Long impresoraId;

    public Ticketera() {
    }

    public Ticketera(Long id, String nombre, Long impresoraId) {
        this.id = id;
        this.nombre = nombre;
        this.impresoraId = impresoraId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getImpresoraId() {
        return impresoraId;
    }

    public void setImpresoraId(Long impresoraId) {
        this.impresoraId = impresoraId;
    }
}
