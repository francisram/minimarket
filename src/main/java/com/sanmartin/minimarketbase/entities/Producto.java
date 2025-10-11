package com.sanmartin.minimarketbase.entities;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "codigo_barra", nullable = false, unique = true, length = 50)
    private String codigoBarra;

    @Column(name = "codigo_interno", length = 50)
    private String codigoInterno;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "id_categoria", nullable = false)
    private Integer idCategoria;

    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "id_impuesto")
    private Integer idImpuesto;

    @Column(name = "precio_costo", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Column(name = "precio_mayorista", precision = 12, scale = 2)
    private BigDecimal precioMayorista;

    @Column(name = "descuento_maximo", precision = 5, scale = 2)
    private BigDecimal descuentoMaximo;

    @Column(name = "stock_minimo", columnDefinition = "integer default 0")
    private Integer stockMinimo;

    @Column(name = "unidad_medida", length = 20, nullable = false)
    private String unidadMedida;

    @Column(name = "peso_neto", precision = 10, scale = 3)
    private BigDecimal pesoNeto;

    @Column(precision = 10, scale = 3)
    private BigDecimal volumen;

    @Column(length = 1)
    private String estado = "A";

    @Column(name = "es_perecedero")
    private Boolean esPerecedero = false;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    @Column(name = "proveedor_principal")
    private Integer proveedorPrincipal;

    @Column(name = "ubicacion_gondola", length = 50)
    private String ubicacionGondola;

    // Getters y setters
    // (puedes usar Lombok si prefieres)
}
