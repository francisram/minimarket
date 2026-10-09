package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PresentacionRepository presentacionRepository;
    private final SaborRepository saborRepository;
    private final ToppingRepository toppingRepository;
    private final ProductoSimpleRepository productoSimpleRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         PresentacionRepository presentacionRepository,
                         SaborRepository saborRepository,
                         ToppingRepository toppingRepository,
                         ProductoSimpleRepository productoSimpleRepository) {
        this.pedidoRepository = pedidoRepository;
        this.presentacionRepository = presentacionRepository;
        this.saborRepository = saborRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ReglaDeNegocioException("El pedido debe contener al menos un ítem.");
        }

        Pedido pedido = Pedido.builder()
                .clienteNombre(request.getClienteNombre())
                .metodoPago(request.getMetodoPago())
                .tipoEntrega(request.getTipoEntrega())
                .notas(request.getNotas())
                .estado(EstadoPedido.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal granTotal = BigDecimal.ZERO;

        for (DetallePedidoRequestDTO itemDto : request.getItems()) {
            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .tipoItem(itemDto.getTipoItem())
                    .cantidad(itemDto.getCantidad())
                    .build();

            BigDecimal precioUnitario = BigDecimal.ZERO;

            if (itemDto.getTipoItem() == TipoItemPedido.HELADO) {
                if (itemDto.getPresentacionId() == null) {
                    throw new ReglaDeNegocioException("Debe especificar una presentación para el helado.");
                }

                Presentacion presentacion = presentacionRepository.findById(itemDto.getPresentacionId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Presentación con id " + itemDto.getPresentacionId() + " no encontrada."));

                if (!Boolean.TRUE.equals(presentacion.getActivo())) {
                    throw new ReglaDeNegocioException("La presentación seleccionada ('" + presentacion.getNombre() + "') no está activa.");
                }

                detalle.setPresentacion(presentacion);
                precioUnitario = presentacion.getPrecio();

                // Validar sabores
                List<Long> saborIds = itemDto.getSaborIds();
                if (saborIds == null || saborIds.isEmpty()) {
                    throw new ReglaDeNegocioException("Debe seleccionar al menos un sabor para " + presentacion.getNombre() + ".");
                }

                if (saborIds.size() > presentacion.getMaxSabores()) {
                    throw new ReglaDeNegocioException("La presentación '" + presentacion.getNombre() +
                            "' permite un máximo de " + presentacion.getMaxSabores() +
                            " sabores, pero se enviaron " + saborIds.size() + ".");
                }

                List<Sabor> sabores = new ArrayList<>();
                for (Long saborId : saborIds) {
                    Sabor sabor = saborRepository.findById(saborId)
                            .orElseThrow(() -> new RecursoNoEncontradoException("Sabor con id " + saborId + " no encontrado."));

                    if (!Boolean.TRUE.equals(sabor.getDisponible())) {
                        throw new ReglaDeNegocioException("El sabor '" + sabor.getNombre() + "' no se encuentra disponible actualmente.");
                    }
                    sabores.add(sabor);
                }
                detalle.setSabores(sabores);

                // Validar y sumar toppings si existen
                List<Topping> toppings = new ArrayList<>();
                if (itemDto.getToppingIds() != null && !itemDto.getToppingIds().isEmpty()) {
                    for (Long toppingId : itemDto.getToppingIds()) {
                        Topping topping = toppingRepository.findById(toppingId)
                                .orElseThrow(() -> new RecursoNoEncontradoException("Topping con id " + toppingId + " no encontrado."));

                        if (!Boolean.TRUE.equals(topping.getDisponible())) {
                            throw new ReglaDeNegocioException("El topping '" + topping.getNombre() + "' no está disponible.");
                        }

                        toppings.add(topping);
                        precioUnitario = precioUnitario.add(topping.getPrecioExtra());
                    }
                }
                detalle.setToppings(toppings);

            } else if (itemDto.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE) {
                if (itemDto.getProductoSimpleId() == null) {
                    throw new ReglaDeNegocioException("Debe especificar el id del producto simple.");
                }

                ProductoSimple prod = productoSimpleRepository.findById(itemDto.getProductoSimpleId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto con id " + itemDto.getProductoSimpleId() + " no encontrado."));

                if (!Boolean.TRUE.equals(prod.getActivo())) {
                    throw new ReglaDeNegocioException("El producto '" + prod.getNombre() + "' no está activo.");
                }

                // Control de stock
                if (prod.getStock() != null && prod.getStock() < itemDto.getCantidad()) {
                    throw new ReglaDeNegocioException("Stock insuficiente para '" + prod.getNombre() +
                            "'. Disponible: " + prod.getStock() + ", Solicitado: " + itemDto.getCantidad());
                }

                if (prod.getStock() != null) {
                    prod.setStock(prod.getStock() - itemDto.getCantidad());
                    productoSimpleRepository.save(prod);
                }

                detalle.setProductoSimple(prod);
                precioUnitario = prod.getPrecio();
            }

            BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(itemDto.getCantidad()));
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);

            pedido.getDetalles().add(detalle);
            granTotal = granTotal.add(subtotal);
        }

        pedido.setTotal(granTotal);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar(EstadoPedido estado, LocalDate fecha) {
        if (fecha != null) {
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.atTime(LocalTime.MAX);
            return pedidoRepository.findByFechaCreacionBetweenOrderByFechaCreacionDesc(inicio, fin);
        }

        if (estado != null) {
            return pedidoRepository.findByEstado(estado);
        }

        return pedidoRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con id " + id + " no encontrado"));
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long id) {
        Pedido pedido = obtenerPorId(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaDeNegocioException("El pedido ya se encuentra cancelado.");
        }

        // Reintegrar stock de productos simples si se cancela
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && detalle.getProductoSimple() != null) {
                ProductoSimple prod = detalle.getProductoSimple();
                if (prod.getStock() != null) {
                    prod.setStock(prod.getStock() + detalle.getCantidad());
                    productoSimpleRepository.save(prod);
                }
            }
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }
}
