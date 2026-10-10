package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.util.MonedaPyUtils;
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
    private final TicketPrinterService ticketPrinterService;
    private final SesionCajaRepository sesionCajaRepository;
    private final InstitucionRepository institucionRepository;
    private final ClienteService clienteService;

    public PedidoService(PedidoRepository pedidoRepository,
                         PresentacionRepository presentacionRepository,
                         SaborRepository saborRepository,
                         ToppingRepository toppingRepository,
                         ProductoSimpleRepository productoSimpleRepository,
                         TicketPrinterService ticketPrinterService,
                         SesionCajaRepository sesionCajaRepository,
                         InstitucionRepository institucionRepository,
                         ClienteService clienteService) {
        this.pedidoRepository = pedidoRepository;
        this.presentacionRepository = presentacionRepository;
        this.saborRepository = saborRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.ticketPrinterService = ticketPrinterService;
        this.sesionCajaRepository = sesionCajaRepository;
        this.institucionRepository = institucionRepository;
        this.clienteService = clienteService;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        SesionCaja sesionActiva = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new ReglaDeNegocioException("No se pueden registrar ventas: no existe una sesión de caja abierta."));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ReglaDeNegocioException("El pedido debe contener al menos un ítem.");
        }

        TipoComprobante tipoComprobante = request.getTipoComprobante() != null ? request.getTipoComprobante() : TipoComprobante.TICKET;
        CondicionVenta condicionVenta = request.getCondicionVenta() != null ? request.getCondicionVenta() : CondicionVenta.CONTADO;
        String numeroFactura = null;

        if (tipoComprobante == TipoComprobante.FACTURA) {
            if (request.getClienteRuc() == null || request.getClienteRuc().trim().isEmpty()
                    || request.getClienteNombre() == null || request.getClienteNombre().trim().isEmpty()) {
                throw new ReglaDeNegocioException("Para emitir factura legal debe indicar el RUC y la Razón Social del cliente.");
            }

            Institucion inst = institucionRepository.findById(1L).orElseGet(() -> {
                Institucion nueva = new Institucion();
                nueva.setId(1L);
                nueva.setNombre("Heladería Artesanal");
                return institucionRepository.save(nueva);
            });

            long sig = (inst.getUltimoNumeroFactura() != null ? inst.getUltimoNumeroFactura() : 0L) + 1;
            String estab = (inst.getEstablecimiento() != null && !inst.getEstablecimiento().isBlank()) ? inst.getEstablecimiento() : "001";
            String punto = (inst.getPuntoEmision() != null && !inst.getPuntoEmision().isBlank()) ? inst.getPuntoEmision() : "001";
            numeroFactura = String.format("%s-%s-%07d", estab, punto, sig);

            inst.setUltimoNumeroFactura(sig);
            institucionRepository.save(inst);
        }

        // Auto-guardado o actualización del cliente en la base de datos si se provee RUC
        if (request.getClienteRuc() != null && !request.getClienteRuc().trim().isEmpty()) {
            clienteService.obtenerOCrearClienteDesdeVenta(
                    request.getClienteRuc(),
                    request.getClienteNombre(),
                    request.getClienteDireccion()
            );
        }

        Pedido pedido = Pedido.builder()
                .sesionCaja(sesionActiva)
                .tipoComprobante(tipoComprobante)
                .condicionVenta(condicionVenta)
                .clienteNombre(request.getClienteNombre() != null ? request.getClienteNombre().trim() : null)
                .clienteRuc(request.getClienteRuc() != null ? request.getClienteRuc().trim() : null)
                .clienteDireccion(request.getClienteDireccion() != null ? request.getClienteDireccion().trim() : null)
                .numeroFactura(numeroFactura)
                .metodoPago(request.getMetodoPago())
                .tipoEntrega(request.getTipoEntrega())
                .notas(request.getNotas())
                .estado(EstadoPedido.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .detalles(new ArrayList<>())
                .build();

        BigDecimal granTotal = BigDecimal.ZERO;
        BigDecimal acumuladorExentas = BigDecimal.ZERO;
        BigDecimal acumulador5 = BigDecimal.ZERO;
        BigDecimal acumulador10 = BigDecimal.ZERO;

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
                detalle.setTipoIva(presentacion.getTipoIva() != null ? presentacion.getTipoIva() : TipoIva.IVA_10);
                precioUnitario = presentacion.getPrecio();

                // Control de stock de envases/cucuruchos
                if (presentacion.getStock() != null) {
                    if (presentacion.getStock() < itemDto.getCantidad()) {
                        throw new ReglaDeNegocioException("Stock insuficiente de envases para '" + presentacion.getNombre() +
                                "'. Disponible: " + presentacion.getStock() + ", Solicitado: " + itemDto.getCantidad());
                    }
                    presentacion.setStock(presentacion.getStock() - itemDto.getCantidad());
                    presentacionRepository.save(presentacion);
                }

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

                // Control y descuento de stock en kilos para los sabores seleccionados
                if (presentacion.getPesoGramosAprox() != null && presentacion.getPesoGramosAprox() > 0 && !sabores.isEmpty()) {
                    double kilosPorSabor = Math.round((((double) presentacion.getPesoGramosAprox() * itemDto.getCantidad())
                            / (1000.0 * sabores.size())) * 1000.0) / 1000.0;

                    // Validación previa de stock de cada sabor
                    for (Sabor sabor : sabores) {
                        if (sabor.getStockKilos() != null && sabor.getStockKilos() < kilosPorSabor) {
                            throw new ReglaDeNegocioException(String.format(java.util.Locale.US,
                                    "Stock insuficiente de helado para el sabor '%s'. Disponible: %.2f kg, Solicitado: %.2f kg",
                                    sabor.getNombre(), sabor.getStockKilos(), kilosPorSabor));
                        }
                    }

                    // Descuento atómico
                    for (Sabor sabor : sabores) {
                        if (sabor.getStockKilos() != null) {
                            double nuevoStock = Math.max(0.0, Math.round((sabor.getStockKilos() - kilosPorSabor) * 1000.0) / 1000.0);
                            sabor.setStockKilos(nuevoStock);
                            if (nuevoStock <= 0.001) {
                                sabor.setDisponible(false);
                            }
                            saborRepository.save(sabor);
                        }
                    }
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
                detalle.setTipoIva(prod.getTipoIva() != null ? prod.getTipoIva() : TipoIva.IVA_10);
                precioUnitario = prod.getPrecio();
            }

            BigDecimal subtotal = MonedaPyUtils.redondearGs(precioUnitario.multiply(BigDecimal.valueOf(itemDto.getCantidad())));
            detalle.setPrecioUnitario(MonedaPyUtils.redondearGs(precioUnitario));
            detalle.setSubtotal(subtotal);

            pedido.getDetalles().add(detalle);
            granTotal = MonedaPyUtils.redondearGs(granTotal.add(subtotal));

            TipoIva ivaItem = detalle.getTipoIva() != null ? detalle.getTipoIva() : TipoIva.IVA_10;
            if (ivaItem == TipoIva.EXENTA) {
                acumuladorExentas = acumuladorExentas.add(subtotal);
            } else if (ivaItem == TipoIva.IVA_5) {
                acumulador5 = acumulador5.add(subtotal);
            } else {
                acumulador10 = acumulador10.add(subtotal);
            }
        }

        BigDecimal totalExentas = MonedaPyUtils.redondearGs(acumuladorExentas);
        BigDecimal totalIva5 = MonedaPyUtils.calcularIva5(acumulador5);
        BigDecimal totalGravada5 = MonedaPyUtils.calcularGravada5(acumulador5);
        BigDecimal totalIva10 = MonedaPyUtils.calcularIva10(acumulador10);
        BigDecimal totalGravada10 = MonedaPyUtils.calcularGravada10(acumulador10);
        BigDecimal totalIva = MonedaPyUtils.redondearGs(totalIva5.add(totalIva10));

        pedido.setTotalExentas(totalExentas);
        pedido.setTotalGravada5(totalGravada5);
        pedido.setTotalIva5(totalIva5);
        pedido.setTotalGravada10(totalGravada10);
        pedido.setTotalIva10(totalIva10);
        pedido.setTotalIva(totalIva);
        pedido.setTotal(granTotal);
        Pedido guardado = pedidoRepository.save(pedido);

        if (request.getImpresoraId() != null) {
            ticketPrinterService.imprimirPedidoEnSegundoPlano(request.getImpresoraId(), guardado);
        }

        return guardado;
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

        // Reintegrar stock de productos simples, envases y sabores si se cancela
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && detalle.getProductoSimple() != null) {
                ProductoSimple prod = detalle.getProductoSimple();
                if (prod.getStock() != null) {
                    prod.setStock(prod.getStock() + detalle.getCantidad());
                    productoSimpleRepository.save(prod);
                }
            } else if (detalle.getTipoItem() == TipoItemPedido.HELADO) {
                // Reintegrar envases/cucuruchos
                if (detalle.getPresentacion() != null && detalle.getPresentacion().getStock() != null) {
                    Presentacion pres = detalle.getPresentacion();
                    pres.setStock(pres.getStock() + detalle.getCantidad());
                    presentacionRepository.save(pres);
                }

                // Reintegrar kilos de sabores
                if (detalle.getPresentacion() != null && detalle.getPresentacion().getPesoGramosAprox() != null
                        && detalle.getPresentacion().getPesoGramosAprox() > 0
                        && detalle.getSabores() != null && !detalle.getSabores().isEmpty()) {
                    double kilosPorSabor = Math.round((((double) detalle.getPresentacion().getPesoGramosAprox() * detalle.getCantidad())
                            / (1000.0 * detalle.getSabores().size())) * 1000.0) / 1000.0;

                    for (Sabor sabor : detalle.getSabores()) {
                        if (sabor.getStockKilos() != null) {
                            double nuevoStock = Math.round((sabor.getStockKilos() + kilosPorSabor) * 1000.0) / 1000.0;
                            sabor.setStockKilos(nuevoStock);
                            if (nuevoStock > 0.001 && !Boolean.TRUE.equals(sabor.getDisponible())) {
                                sabor.setDisponible(true);
                            }
                            saborRepository.save(sabor);
                        }
                    }
                }
            }
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public void imprimirTicket(Long pedidoId, Long impresoraId) {
        Pedido pedido = obtenerPorId(pedidoId);
        ticketPrinterService.imprimirPedidoEnSegundoPlano(impresoraId, pedido);
    }
}
