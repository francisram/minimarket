package com.sanmartin.minimarketbase.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data
public class Inventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventario")
    private Integer idInventario;
    
    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;
    
    @Column(name = "stock_actual")
    private Integer stockActual = 0;
    
    @Column(name = "stock_reservado")
    private Integer stockReservado = 0;
    
    @Column(name = "stock_disponible", insertable = false, updatable = false)
    private Integer stockDisponible;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    
    @OneToOne
    @JoinColumn(name = "id_producto", insertable = false, updatable = false)
    private Producto producto;
}