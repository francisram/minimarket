package com.sanmartin.minimarketbase.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "venta")
@Data
public class Venta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;
    
    @Column(name = "id_cliente")
    private Integer idCliente;
    
    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta = LocalDateTime.now();
    
    @Column(name = "total", precision = 12, scale = 2, nullable = false)
    private BigDecimal total;
    
    @Column(name = "metodo_pago", length = 50, nullable = false)
    private String metodoPago;
    
    @Column(name = "estado", length = 20)
    private String estado = "PENDIENTE";
    
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles;
}