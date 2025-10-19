package com.sanmartin.minimarketbase.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventario_movimiento")
@Data
public class InventarioMovimiento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;
    
    @Column(name = "id_producto", nullable = false)
    private Integer idProducto;
    
    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo; // VENTA, COMPRA, AJUSTE, DEVOLUCION
    
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
    
    @Column(name = "motivo", length = 150)
    private String motivo;
    
    @Column(name = "referencia", length = 50)
    private String referencia;
    
    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento = LocalDateTime.now();
    
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;
}