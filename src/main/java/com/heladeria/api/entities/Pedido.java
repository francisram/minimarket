package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoEntrega;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
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
    private SesionCaja sesionCaja;

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

        public Pedido build() {
            return new Pedido(id, fechaCreacion, clienteNombre, metodoPago, tipoEntrega, estado, notas, total, detalles, sesionCaja);
        }
    }
}
