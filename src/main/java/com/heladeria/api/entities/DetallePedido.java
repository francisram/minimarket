package com.heladeria.api.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.heladeria.api.entities.enums.TipoItemPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "detalles_pedido")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonBackReference
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoItemPedido tipoItem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "presentacion_id")
    private Presentacion presentacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "detalle_pedido_sabores",
        joinColumns = @JoinColumn(name = "detalle_id"),
        inverseJoinColumns = @JoinColumn(name = "sabor_id")
    )
    private List<Sabor> sabores = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "detalle_pedido_toppings",
        joinColumns = @JoinColumn(name = "detalle_id"),
        inverseJoinColumns = @JoinColumn(name = "topping_id")
    )
    private List<Topping> toppings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_simple_id")
    private ProductoSimple productoSimple;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal subtotal;

    public DetallePedido() {
    }

    public DetallePedido(Long id, Pedido pedido, TipoItemPedido tipoItem, Presentacion presentacion,
                         List<Sabor> sabores, List<Topping> toppings, ProductoSimple productoSimple,
                         Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.id = id;
        this.pedido = pedido;
        this.tipoItem = tipoItem;
        this.presentacion = presentacion;
        this.sabores = sabores != null ? sabores : new ArrayList<>();
        this.toppings = toppings != null ? toppings : new ArrayList<>();
        this.productoSimple = productoSimple;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario != null ? precioUnitario.setScale(0, java.math.RoundingMode.HALF_UP) : null;
        this.subtotal = subtotal != null ? subtotal.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public static DetallePedidoBuilder builder() {
        return new DetallePedidoBuilder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public TipoItemPedido getTipoItem() { return tipoItem; }
    public void setTipoItem(TipoItemPedido tipoItem) { this.tipoItem = tipoItem; }

    public Presentacion getPresentacion() { return presentacion; }
    public void setPresentacion(Presentacion presentacion) { this.presentacion = presentacion; }

    public List<Sabor> getSabores() { return sabores; }
    public void setSabores(List<Sabor> sabores) { this.sabores = sabores; }

    public List<Topping> getToppings() { return toppings; }
    public void setToppings(List<Topping> toppings) { this.toppings = toppings; }

    public ProductoSimple getProductoSimple() { return productoSimple; }
    public void setProductoSimple(ProductoSimple productoSimple) { this.productoSimple = productoSimple; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario != null ? precioUnitario.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal != null ? subtotal.setScale(0, java.math.RoundingMode.HALF_UP) : null;
    }

    public static class DetallePedidoBuilder {
        private Long id;
        private Pedido pedido;
        private TipoItemPedido tipoItem;
        private Presentacion presentacion;
        private List<Sabor> sabores = new ArrayList<>();
        private List<Topping> toppings = new ArrayList<>();
        private ProductoSimple productoSimple;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public DetallePedidoBuilder id(Long id) { this.id = id; return this; }
        public DetallePedidoBuilder pedido(Pedido pedido) { this.pedido = pedido; return this; }
        public DetallePedidoBuilder tipoItem(TipoItemPedido tipoItem) { this.tipoItem = tipoItem; return this; }
        public DetallePedidoBuilder presentacion(Presentacion presentacion) { this.presentacion = presentacion; return this; }
        public DetallePedidoBuilder sabores(List<Sabor> sabores) { this.sabores = sabores; return this; }
        public DetallePedidoBuilder toppings(List<Topping> toppings) { this.toppings = toppings; return this; }
        public DetallePedidoBuilder productoSimple(ProductoSimple productoSimple) { this.productoSimple = productoSimple; return this; }
        public DetallePedidoBuilder cantidad(Integer cantidad) { this.cantidad = cantidad; return this; }
        public DetallePedidoBuilder precioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; return this; }
        public DetallePedidoBuilder subtotal(BigDecimal subtotal) { this.subtotal = subtotal; return this; }

        public DetallePedido build() {
            return new DetallePedido(id, pedido, tipoItem, presentacion, sabores, toppings, productoSimple, cantidad, precioUnitario, subtotal);
        }
    }
}
