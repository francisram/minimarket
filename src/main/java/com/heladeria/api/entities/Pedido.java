package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoEntrega;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(length = 100)
    private String clienteNombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MetodoPago metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEntrega tipoEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado;

    @Column(length = 255)
    private String notas;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_caja_id")
    @JsonIgnore
    private SesionCaja sesionCaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComprobante tipoComprobante = TipoComprobante.TICKET;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CondicionVenta condicionVenta = CondicionVenta.CONTADO;

    @Column(length = 30)
    private String clienteRuc;

    @Column(length = 150)
    private String clienteDireccion;

    @Column(length = 30)
    private String numeroFactura;

    @Column(name = "total_exentas", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalExentas = BigDecimal.ZERO;

    @Column(name = "total_gravada5", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalGravada5 = BigDecimal.ZERO;

    @Column(name = "total_gravada10", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalGravada10 = BigDecimal.ZERO;

    @Column(name = "total_iva5", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalIva5 = BigDecimal.ZERO;

    @Column(name = "total_iva10", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalIva10 = BigDecimal.ZERO;

    @Column(name = "total_iva", nullable = false, precision = 12, scale = 0, columnDefinition = "numeric(12,0) default 0")
    private BigDecimal totalIva = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public Pedido(Long id, LocalDateTime fechaCreacion, String clienteNombre, MetodoPago metodoPago,
                  TipoEntrega tipoEntrega, EstadoPedido estado, String notas, BigDecimal total, List<DetallePedido> detalles) {
        this(id, fechaCreacion, clienteNombre, metodoPago, tipoEntrega, estado, notas, total, detalles, null);
    }

    public Pedido(Long id, LocalDateTime fechaCreacion, String clienteNombre, MetodoPago metodoPago,
                  TipoEntrega tipoEntrega, EstadoPedido estado, String notas, BigDecimal total, List<DetallePedido> detalles,
                  SesionCaja sesionCaja) {
        this(id, fechaCreacion, clienteNombre, metodoPago, tipoEntrega, estado, notas, total, detalles, sesionCaja,
                TipoComprobante.TICKET, CondicionVenta.CONTADO, null, null, null);
    }

    public Pedido(Long id, LocalDateTime fechaCreacion, String clienteNombre, MetodoPago metodoPago,
                  TipoEntrega tipoEntrega, EstadoPedido estado, String notas, BigDecimal total, List<DetallePedido> detalles,
                  SesionCaja sesionCaja, TipoComprobante tipoComprobante, CondicionVenta condicionVenta,
                  String clienteRuc, String clienteDireccion, String numeroFactura) {
        this(id, fechaCreacion, clienteNombre, metodoPago, tipoEntrega, estado, notas, total, detalles,
                sesionCaja, tipoComprobante, condicionVenta, clienteRuc, clienteDireccion, numeroFactura,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Pedido(Long id, LocalDateTime fechaCreacion, String clienteNombre, MetodoPago metodoPago,
                  TipoEntrega tipoEntrega, EstadoPedido estado, String notas, BigDecimal total, List<DetallePedido> detalles,
                  SesionCaja sesionCaja, TipoComprobante tipoComprobante, CondicionVenta condicionVenta,
                  String clienteRuc, String clienteDireccion, String numeroFactura,
                  BigDecimal totalExentas, BigDecimal totalGravada5, BigDecimal totalGravada10,
                  BigDecimal totalIva5, BigDecimal totalIva10, BigDecimal totalIva) {
        this.id = id;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.clienteNombre = clienteNombre;
        this.metodoPago = metodoPago;
        this.tipoEntrega = tipoEntrega;
        this.estado = estado != null ? estado : EstadoPedido.PENDIENTE;
        this.notas = notas;
        this.total = total != null ? total.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        this.sesionCaja = sesionCaja;
        this.tipoComprobante = tipoComprobante != null ? tipoComprobante : TipoComprobante.TICKET;
        this.condicionVenta = condicionVenta != null ? condicionVenta : CondicionVenta.CONTADO;
        this.clienteRuc = clienteRuc;
        this.clienteDireccion = clienteDireccion;
        this.numeroFactura = numeroFactura;
        this.totalExentas = totalExentas != null ? totalExentas.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalGravada5 = totalGravada5 != null ? totalGravada5.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalGravada10 = totalGravada10 != null ? totalGravada10.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalIva5 = totalIva5 != null ? totalIva5.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalIva10 = totalIva10 != null ? totalIva10.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.totalIva = totalIva != null ? totalIva.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public static PedidoBuilder builder() {
        return new PedidoBuilder();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPedido.PENDIENTE;
        }
        if (this.tipoComprobante == null) {
            this.tipoComprobante = TipoComprobante.TICKET;
        }
        if (this.condicionVenta == null) {
            this.condicionVenta = CondicionVenta.CONTADO;
        }
        if (this.totalExentas == null) {
            this.totalExentas = BigDecimal.ZERO;
        }
        if (this.totalGravada5 == null) {
            this.totalGravada5 = BigDecimal.ZERO;
        }
        if (this.totalGravada10 == null) {
            this.totalGravada10 = BigDecimal.ZERO;
        }
        if (this.totalIva5 == null) {
            this.totalIva5 = BigDecimal.ZERO;
        }
        if (this.totalIva10 == null) {
            this.totalIva10 = BigDecimal.ZERO;
        }
        if (this.totalIva == null) {
            this.totalIva = BigDecimal.ZERO;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public TipoEntrega getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(TipoEntrega tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) {
        this.total = total != null ? total.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }

    public SesionCaja getSesionCaja() { return sesionCaja; }
    public void setSesionCaja(SesionCaja sesionCaja) { this.sesionCaja = sesionCaja; }

    @Transient
    @JsonProperty("sesionCajaId")
    public Long getSesionCajaId() {
        return sesionCaja != null ? sesionCaja.getId() : null;
    }

    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public CondicionVenta getCondicionVenta() { return condicionVenta; }
    public void setCondicionVenta(CondicionVenta condicionVenta) { this.condicionVenta = condicionVenta; }

    public String getClienteRuc() { return clienteRuc; }
    public void setClienteRuc(String clienteRuc) { this.clienteRuc = clienteRuc; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public BigDecimal getTotalExentas() { return totalExentas; }
    public void setTotalExentas(BigDecimal totalExentas) {
        this.totalExentas = totalExentas != null ? totalExentas.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalGravada5() { return totalGravada5; }
    public void setTotalGravada5(BigDecimal totalGravada5) {
        this.totalGravada5 = totalGravada5 != null ? totalGravada5.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalGravada10() { return totalGravada10; }
    public void setTotalGravada10(BigDecimal totalGravada10) {
        this.totalGravada10 = totalGravada10 != null ? totalGravada10.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIva5() { return totalIva5; }
    public void setTotalIva5(BigDecimal totalIva5) {
        this.totalIva5 = totalIva5 != null ? totalIva5.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIva10() { return totalIva10; }
    public void setTotalIva10(BigDecimal totalIva10) {
        this.totalIva10 = totalIva10 != null ? totalIva10.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public BigDecimal getTotalIva() { return totalIva; }
    public void setTotalIva(BigDecimal totalIva) {
        this.totalIva = totalIva != null ? totalIva.setScale(0, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    public static class PedidoBuilder {
        private Long id;
        private LocalDateTime fechaCreacion;
        private String clienteNombre;
        private MetodoPago metodoPago;
        private TipoEntrega tipoEntrega;
        private EstadoPedido estado;
        private String notas;
        private BigDecimal total = BigDecimal.ZERO;
        private List<DetallePedido> detalles = new ArrayList<>();
        private SesionCaja sesionCaja;
        private TipoComprobante tipoComprobante = TipoComprobante.TICKET;
        private CondicionVenta condicionVenta = CondicionVenta.CONTADO;
        private String clienteRuc;
        private String clienteDireccion;
        private String numeroFactura;
        private BigDecimal totalExentas = BigDecimal.ZERO;
        private BigDecimal totalGravada5 = BigDecimal.ZERO;
        private BigDecimal totalGravada10 = BigDecimal.ZERO;
        private BigDecimal totalIva5 = BigDecimal.ZERO;
        private BigDecimal totalIva10 = BigDecimal.ZERO;
        private BigDecimal totalIva = BigDecimal.ZERO;

        public PedidoBuilder id(Long id) { this.id = id; return this; }
        public PedidoBuilder fechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; return this; }
        public PedidoBuilder clienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; return this; }
        public PedidoBuilder metodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; return this; }
        public PedidoBuilder tipoEntrega(TipoEntrega tipoEntrega) { this.tipoEntrega = tipoEntrega; return this; }
        public PedidoBuilder estado(EstadoPedido estado) { this.estado = estado; return this; }
        public PedidoBuilder notas(String notas) { this.notas = notas; return this; }
        public PedidoBuilder total(BigDecimal total) { this.total = total; return this; }
        public PedidoBuilder detalles(List<DetallePedido> detalles) { this.detalles = detalles; return this; }
        public PedidoBuilder sesionCaja(SesionCaja sesionCaja) { this.sesionCaja = sesionCaja; return this; }
        public PedidoBuilder tipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; return this; }
        public PedidoBuilder condicionVenta(CondicionVenta condicionVenta) { this.condicionVenta = condicionVenta; return this; }
        public PedidoBuilder clienteRuc(String clienteRuc) { this.clienteRuc = clienteRuc; return this; }
        public PedidoBuilder clienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion; return this; }
        public PedidoBuilder numeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; return this; }
        public PedidoBuilder totalExentas(BigDecimal totalExentas) { this.totalExentas = totalExentas; return this; }
        public PedidoBuilder totalGravada5(BigDecimal totalGravada5) { this.totalGravada5 = totalGravada5; return this; }
        public PedidoBuilder totalGravada10(BigDecimal totalGravada10) { this.totalGravada10 = totalGravada10; return this; }
        public PedidoBuilder totalIva5(BigDecimal totalIva5) { this.totalIva5 = totalIva5; return this; }
        public PedidoBuilder totalIva10(BigDecimal totalIva10) { this.totalIva10 = totalIva10; return this; }
        public PedidoBuilder totalIva(BigDecimal totalIva) { this.totalIva = totalIva; return this; }

        public Pedido build() {
            return new Pedido(id, fechaCreacion, clienteNombre, metodoPago, tipoEntrega, estado, notas, total, detalles,
                    sesionCaja, tipoComprobante, condicionVenta, clienteRuc, clienteDireccion, numeroFactura,
                    totalExentas, totalGravada5, totalGravada10, totalIva5, totalIva10, totalIva);
        }
    }
}
