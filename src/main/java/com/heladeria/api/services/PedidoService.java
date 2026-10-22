package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.TipoArticulo;
import java.util.Optional;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.entities.enums.TipoItemStock;
import com.heladeria.api.entities.enums.TipoMovimientoStock;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.dto.CancelarPedidoRequestDTO;
import com.heladeria.api.util.MonedaPyUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public PedidoService(PedidoRepository pedidoRepository,
                         PresentacionRepository presentacionRepository,
                         SaborRepository saborRepository,
                         ToppingRepository toppingRepository,
                         ProductoSimpleRepository productoSimpleRepository,
                         TicketPrinterService ticketPrinterService,
                         SesionCajaRepository sesionCajaRepository,
                         InstitucionRepository institucionRepository,
                         ClienteService clienteService,
                         InventarioService inventarioService,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         AuditoriaService auditoriaService) {
        this.pedidoRepository = pedidoRepository;
        this.presentacionRepository = presentacionRepository;
        this.saborRepository = saborRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.ticketPrinterService = ticketPrinterService;
        this.sesionCajaRepository = sesionCajaRepository;
        this.institucionRepository = institucionRepository;
        this.clienteService = clienteService;
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
        return crearPedido(request, username);
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request, String username) {
        if (username == null || username.isBlank()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            username = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
        }

        String finalUsername = username;
        SesionCaja sesionActiva = sesionCajaRepository
                .findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(finalUsername, EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new ReglaDeNegocioException("El usuario '" + finalUsername + "' no tiene una sesión de caja abierta para registrar ventas."));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ReglaDeNegocioException("El pedido debe contener al menos un ítem.");
        }

        TipoComprobante tipoComprobante = request.getTipoComprobante() != null ? request.getTipoComprobante() : TipoComprobante.TICKET;
        CondicionVenta condicionVenta = request.getCondicionVenta() != null ? request.getCondicionVenta() : CondicionVenta.CONTADO;
        String numeroFactura = null;

        if ((request.getMetodoPago() == MetodoPago.TARJETA_DEBITO || request.getMetodoPago() == MetodoPago.TARJETA_CREDITO)
                && (request.getNumeroVoucher() == null || request.getNumeroVoucher().trim().isEmpty())) {
            throw new ReglaDeNegocioException("El número de voucher / boleta es obligatorio para pagos con tarjeta de débito o crédito.");
        }

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

            // Validar Vencimiento del Timbrado
            if (inst.getTimbradoVencimiento() != null && inst.getTimbradoVencimiento().isBefore(LocalDate.now())) {
                throw new IllegalStateException("El timbrado fiscal N° " + inst.getTimbrado() + " ha vencido el " + inst.getTimbradoVencimiento() + ". Debe renovar el timbrado para poder emitir facturas legales.");
            }

            long sig = (inst.getUltimoNumeroFactura() != null ? inst.getUltimoNumeroFactura() : 0L) + 1;

            // Validar Límite del Talonario
            if (inst.getNumeroFinFactura() != null && sig > inst.getNumeroFinFactura()) {
                throw new IllegalStateException("Se ha alcanzado el límite máximo del talonario (" + inst.getNumeroFinFactura() + "). Debe habilitar un nuevo talonario desde el módulo contable.");
            }

            String estab = (inst.getEstablecimiento() != null && !inst.getEstablecimiento().isBlank()) ? inst.getEstablecimiento() : "001";
            String punto = (inst.getPuntoEmision() != null && !inst.getPuntoEmision().isBlank()) ? inst.getPuntoEmision() : "001";
            numeroFactura = String.format("%s-%s-%07d", estab, punto, sig);

            inst.setUltimoNumeroFactura(sig);
            institucionRepository.save(inst);
        }
 
        String clienteRuc = (request.getClienteRuc() != null && !request.getClienteRuc().trim().isEmpty())
                ? request.getClienteRuc().trim()
                : null;
        if (clienteRuc != null && clienteRuc.length() > 30) {
            clienteRuc = clienteRuc.substring(0, 30);
        }

        String clienteNombre = (request.getClienteNombre() != null && !request.getClienteNombre().trim().isEmpty())
                ? request.getClienteNombre().trim()
                : null;
        if (clienteNombre != null && clienteNombre.length() > 100) {
            clienteNombre = clienteNombre.substring(0, 100);
        }

        String clienteDireccion = (request.getClienteDireccion() != null && !request.getClienteDireccion().trim().isEmpty())
                ? request.getClienteDireccion().trim()
                : null;
        if (clienteDireccion != null && clienteDireccion.length() > 150) {
            clienteDireccion = clienteDireccion.substring(0, 150);
        }

        // Auto-guardado o actualización del cliente en la base de datos si se provee RUC
        if (clienteRuc != null) {
            clienteService.obtenerOCrearClienteDesdeVenta(
                    clienteRuc,
                    clienteNombre,
                    clienteDireccion
            );
        }

        String trxId = (request.getTrxId() != null && !request.getTrxId().isBlank())
                ? request.getTrxId().trim()
                : "TRX-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Pedido pedido = Pedido.builder()
                .sesionCaja(sesionActiva)
                .tipoComprobante(tipoComprobante)
                .condicionVenta(condicionVenta)
                .clienteNombre(clienteNombre)
                .clienteRuc(clienteRuc)
                .clienteDireccion(clienteDireccion)
                .numeroFactura(numeroFactura)
                .numeroVoucher(request.getNumeroVoucher() != null ? request.getNumeroVoucher().trim() : null)
                .trxId(trxId)
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
                    double stockAnt = presentacion.getStock().doubleValue();
                    double stockPost = stockAnt - itemDto.getCantidad();
                    presentacion.setStock((int) Math.round(stockPost));
                    presentacionRepository.save(presentacion);
                    inventarioService.registrarMovimientoInterno(
                            TipoItemStock.ENVASE,
                            presentacion.getId(),
                            presentacion.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA,
                            itemDto.getCantidad().doubleValue(),
                            stockAnt,
                            stockPost,
                            "Venta POS #" + (numeroFactura != null ? numeroFactura : trxId),
                            username
                    );
                    presentacionRepository.save(presentacion);
                }

                // Validar sabores (opcionales para venta ágil en POS)
                List<Long> saborIds = itemDto.getSaborIds();
                List<Sabor> sabores = new ArrayList<>();

                if (saborIds != null && !saborIds.isEmpty()) {
                    if (saborIds.size() > presentacion.getMaxSabores()) {
                        throw new ReglaDeNegocioException("La presentación '" + presentacion.getNombre() +
                                "' permite un máximo de " + presentacion.getMaxSabores() +
                                " sabores, pero se enviaron " + saborIds.size() + ".");
                    }

                    for (Long saborId : saborIds) {
                        Sabor sabor = saborRepository.findById(saborId)
                                .orElseThrow(() -> new RecursoNoEncontradoException("Sabor con id " + saborId + " no encontrado."));

                        if (!Boolean.TRUE.equals(sabor.getDisponible())) {
                            throw new ReglaDeNegocioException("El sabor '" + sabor.getNombre() + "' no se encuentra disponible actualmente.");
                        }
                        sabores.add(sabor);
                    }
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
                            double stockAnt = sabor.getStockKilos();
                            double nuevoStock = Math.max(0.0, Math.round((sabor.getStockKilos() - kilosPorSabor) * 1000.0) / 1000.0);
                            sabor.setStockKilos(nuevoStock);
                            if (nuevoStock <= 0.001) {
                                sabor.setDisponible(false);
                            }
                            saborRepository.save(sabor);
                            inventarioService.registrarMovimientoInterno(
                                    TipoItemStock.SABOR,
                                    sabor.getId(),
                                    sabor.getNombre(),
                                    TipoMovimientoStock.SALIDA_VENTA,
                                    kilosPorSabor,
                                    stockAnt,
                                    nuevoStock,
                                    "Venta POS #" + (numeroFactura != null ? numeroFactura : trxId),
                                    username
                            );
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

                // Verificar si este producto es un COMBO con componentes registrados
                Optional<Producto> optCombo = Optional.empty();
                if (prod.getCodigo() != null && !prod.getCodigo().isBlank()) {
                    optCombo = productoRepository.findBySkuIgnoreCase(prod.getCodigo().trim());
                }
                if (optCombo.isEmpty() && prod.getCodigoBarra() != null && !prod.getCodigoBarra().isBlank()) {
                    optCombo = productoRepository.findByCodigoBarraIgnoreCase(prod.getCodigoBarra().trim());
                }
                if (optCombo.isEmpty()) {
                    optCombo = productoRepository.findByNombreIgnoreCase(prod.getNombre().trim());
                }

                if (optCombo.isPresent() && optCombo.get().getTipoArticulo() == TipoArticulo.COMBO
                        && optCombo.get().getComponentes() != null && !optCombo.get().getComponentes().isEmpty()) {
                    // Es un Combo Compuesto: Descontar stock de cada uno de sus componentes individuales
                    Producto combo = optCombo.get();
                    for (ComboComponente cc : combo.getComponentes()) {
                        Producto comp = cc.getComponente();
                        BigDecimal cantFactor = cc.getCantidad() != null ? cc.getCantidad() : BigDecimal.ONE;
                        BigDecimal cantTotalDescontar = cantFactor.multiply(BigDecimal.valueOf(itemDto.getCantidad()));

                        // Descontar en Producto maestro
                        if (comp.getStock() != null) {
                            double compStockAnt = comp.getStock().doubleValue();
                            double compStockPost = compStockAnt - cantTotalDescontar.doubleValue();
                            comp.setStock(BigDecimal.valueOf(compStockPost));
                            productoRepository.save(comp);
                        }

                        // Descontar en catalogo operativo segun el tipo sin lambdas
                        String conceptoCombo = "Venta Combo #" + (numeroFactura != null ? numeroFactura : trxId) + " (" + prod.getNombre() + ")";
                        if (comp.getTipoArticulo() == TipoArticulo.ENVASE) {
                            Optional<Presentacion> optPres = presentacionRepository.findByNombreIgnoreCase(comp.getNombre().trim());
                            if (optPres.isPresent()) {
                                Presentacion presComp = optPres.get();
                                if (presComp.getStock() != null) {
                                    double pAnt = presComp.getStock().doubleValue();
                                    double pPost = pAnt - cantTotalDescontar.doubleValue();
                                    presComp.setStock((int) Math.round(pPost));
                                    presentacionRepository.save(presComp);
                                    inventarioService.registrarMovimientoInterno(
                                            TipoItemStock.ENVASE,
                                            presComp.getId(),
                                            presComp.getNombre(),
                                            TipoMovimientoStock.SALIDA_VENTA,
                                            cantTotalDescontar.doubleValue(),
                                            pAnt,
                                            pPost,
                                            conceptoCombo,
                                            finalUsername
                                    );
                                }
                            }
                        } else if (comp.getTipoArticulo() == TipoArticulo.TOPPING) {
                            Optional<Topping> optTop = toppingRepository.findByNombreIgnoreCase(comp.getNombre().trim());
                            if (optTop.isPresent()) {
                                Topping topComp = optTop.get();
                                inventarioService.registrarMovimientoInterno(
                                        TipoItemStock.PRODUCTO,
                                        topComp.getId(),
                                        topComp.getNombre(),
                                        TipoMovimientoStock.SALIDA_VENTA,
                                        cantTotalDescontar.doubleValue(),
                                        0.0,
                                        0.0,
                                        conceptoCombo,
                                        finalUsername
                                );
                            }
                        } else if (comp.getTipoArticulo() == TipoArticulo.SABOR) {
                            Optional<Sabor> optSab = saborRepository.findByNombreIgnoreCase(comp.getNombre().trim());
                            if (optSab.isPresent()) {
                                Sabor sabComp = optSab.get();
                                if (sabComp.getStockKilos() != null) {
                                    double sAnt = sabComp.getStockKilos();
                                    double sPost = Math.max(0.0, sAnt - cantTotalDescontar.doubleValue());
                                    sabComp.setStockKilos(sPost);
                                    if (sPost <= 0.001) sabComp.setDisponible(false);
                                    saborRepository.save(sabComp);
                                    inventarioService.registrarMovimientoInterno(
                                            TipoItemStock.SABOR,
                                            sabComp.getId(),
                                            sabComp.getNombre(),
                                            TipoMovimientoStock.SALIDA_VENTA,
                                            cantTotalDescontar.doubleValue(),
                                            sAnt,
                                            sPost,
                                            conceptoCombo,
                                            finalUsername
                                    );
                                }
                            }
                        } else {
                            // VENTA_DIRECTA, INSUMO, ELABORADO
                            Optional<ProductoSimple> optPs = productoSimpleRepository.findByNombreIgnoreCase(comp.getNombre().trim());
                            if (optPs.isPresent()) {
                                ProductoSimple psComp = optPs.get();
                                if (psComp.getStock() != null) {
                                    double psAnt = psComp.getStock().doubleValue();
                                    double psPost = psAnt - cantTotalDescontar.doubleValue();
                                    psComp.setStock((int) Math.round(psPost));
                                    productoSimpleRepository.save(psComp);
                                    inventarioService.registrarMovimientoInterno(
                                            TipoItemStock.PRODUCTO,
                                            psComp.getId(),
                                            psComp.getNombre(),
                                            TipoMovimientoStock.SALIDA_VENTA,
                                            cantTotalDescontar.doubleValue(),
                                            psAnt,
                                            psPost,
                                            conceptoCombo,
                                            finalUsername
                                    );
                                }
                            }
                        }
                    }
                } else {
                    // Control de stock estandar (Combo simple o producto unitario)
                    if (prod.getStock() != null && prod.getStock() < itemDto.getCantidad()) {
                        throw new ReglaDeNegocioException("Stock insuficiente para '" + prod.getNombre() +
                                "'. Disponible: " + prod.getStock() + ", Solicitado: " + itemDto.getCantidad());
                    }

                    if (prod.getStock() != null) {
                        double stockAnt = prod.getStock().doubleValue();
                        double stockPost = stockAnt - itemDto.getCantidad();
                        prod.setStock((int) Math.round(stockPost));
                        productoSimpleRepository.save(prod);
                        inventarioService.registrarMovimientoInterno(
                                TipoItemStock.PRODUCTO,
                                prod.getId(),
                                prod.getNombre(),
                                TipoMovimientoStock.SALIDA_VENTA,
                                itemDto.getCantidad().doubleValue(),
                                stockAnt,
                                stockPost,
                                "Venta POS #" + (numeroFactura != null ? numeroFactura : trxId),
                                username
                        );
                        productoSimpleRepository.save(prod);
                    }
                }

                detalle.setProductoSimple(prod);
                detalle.setTipoIva(prod.getTipoIva() != null ? prod.getTipoIva() : TipoIva.IVA_10);
                precioUnitario = prod.getPrecio();
            } else if (itemDto.getTipoItem() == TipoItemPedido.TOPPING) {
                Long toppingId = (itemDto.getToppingIds() != null && !itemDto.getToppingIds().isEmpty())
                        ? itemDto.getToppingIds().get(0)
                        : null;
                if (toppingId == null) {
                    throw new ReglaDeNegocioException("Debe especificar el id del topping.");
                }
                Topping topping = toppingRepository.findById(toppingId)
                        .orElseThrow(() -> new RecursoNoEncontradoException("Topping con id " + toppingId + " no encontrado."));

                if (!Boolean.TRUE.equals(topping.getDisponible())) {
                    throw new ReglaDeNegocioException("El topping '" + topping.getNombre() + "' no está disponible actualmente.");
                }

                detalle.setToppings(List.of(topping));
                detalle.setTipoIva(TipoIva.IVA_10);
                precioUnitario = topping.getPrecioExtra();
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

    @Transactional(readOnly = true)
    public Pedido obtenerPorTrxId(String trxId) {
        if (trxId == null || trxId.isBlank()) {
            throw new RecursoNoEncontradoException("Identificador trxId no válido");
        }
        return pedidoRepository.findByTrxId(trxId.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido con trxId '" + trxId + "' no encontrado"));
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long id) {
        return cancelarPedido(id, null);
    }

    @Transactional
    public Pedido cancelarPedido(Long id, CancelarPedidoRequestDTO dto) {
        Pedido pedido = obtenerPorId(id);

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaDeNegocioException("El pedido ya se encuentra cancelado.");
        }

        var authUser = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdminLogueado = authUser != null && authUser.getAuthorities() != null && authUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_OWNER"));

        String usuarioCancelacion;

        // Validar contraseña de administrador / supervisor
        if (dto != null && dto.getAdminPassword() != null && !dto.getAdminPassword().trim().isEmpty()) {
            List<Usuario> admins = usuarioRepository.findAll().stream()
                    .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                    .filter(u -> u.getRol() != null && ("ADMIN".equalsIgnoreCase(u.getRol().getNombreRol()) || "OWNER".equalsIgnoreCase(u.getRol().getNombreRol())))
                    .toList();

            Usuario adminAutorizante = null;
            if (dto.getAdminUsername() != null && !dto.getAdminUsername().isBlank()) {
                adminAutorizante = admins.stream()
                        .filter(u -> u.getUsername().equalsIgnoreCase(dto.getAdminUsername().trim()))
                        .findFirst()
                        .filter(u -> passwordEncoder.matches(dto.getAdminPassword(), u.getPasswordHash()))
                        .orElse(null);
            } else {
                for (Usuario u : admins) {
                    if (passwordEncoder.matches(dto.getAdminPassword(), u.getPasswordHash())) {
                        adminAutorizante = u;
                        break;
                    }
                }
            }

            if (adminAutorizante == null) {
                throw new ReglaDeNegocioException("Contraseña de Administrador incorrecta. Operación de cancelación rechazada.");
            }

            usuarioCancelacion = adminAutorizante.getUsername();
        } else if (esAdminLogueado) {
            usuarioCancelacion = authUser.getName();
        } else {
            throw new ReglaDeNegocioException("Se requiere la contraseña de un Administrador o Propietario para anular un comprobante.");
        }

        String docRef = pedido.getNumeroFactura() != null ? pedido.getNumeroFactura() : (pedido.getTrxId() != null ? pedido.getTrxId() : String.valueOf(pedido.getId()));
        String motivoStr = (dto != null && dto.getMotivo() != null && !dto.getMotivo().isBlank())
                ? " - Motivo: " + dto.getMotivo().trim()
                : "";
        String detalleLog = "Cancelación Pedido #" + docRef + " (Autorizado por Admin: " + usuarioCancelacion + ")" + motivoStr;

        if (pedido.getSesionCaja() != null && pedido.getSesionCaja().getEstado() == EstadoSesionCaja.CERRADA) {
            detalleLog += " [Sesión de Caja #" + pedido.getSesionCaja().getId() + " CERRADA]";
        }

        // Reintegrar stock de productos simples, envases y sabores si se cancela
        for (DetallePedido detalle : pedido.getDetalles()) {
            if (detalle.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && detalle.getProductoSimple() != null) {
                ProductoSimple prod = detalle.getProductoSimple();
                if (prod.getStock() != null) {
                    double stockAnt = prod.getStock().doubleValue();
                    double stockPost = stockAnt + detalle.getCantidad();
                    prod.setStock((int) Math.round(stockPost));
                    productoSimpleRepository.save(prod);
                    inventarioService.registrarMovimientoInterno(
                            TipoItemStock.PRODUCTO,
                            prod.getId(),
                            prod.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION,
                            detalle.getCantidad().doubleValue(),
                            stockAnt,
                            stockPost,
                            detalleLog,
                            usuarioCancelacion
                    );
                }
            } else if (detalle.getTipoItem() == TipoItemPedido.HELADO) {
                // Reintegrar envases/cucuruchos
                if (detalle.getPresentacion() != null && detalle.getPresentacion().getStock() != null) {
                    Presentacion pres = detalle.getPresentacion();
                    double stockAnt = pres.getStock().doubleValue();
                    double stockPost = stockAnt + detalle.getCantidad();
                    pres.setStock((int) Math.round(stockPost));
                    presentacionRepository.save(pres);
                    inventarioService.registrarMovimientoInterno(
                            TipoItemStock.ENVASE,
                            pres.getId(),
                            pres.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION,
                            detalle.getCantidad().doubleValue(),
                            stockAnt,
                            stockPost,
                            detalleLog,
                            usuarioCancelacion
                    );
                }

                // Reintegrar kilos de sabores
                if (detalle.getPresentacion() != null && detalle.getPresentacion().getPesoGramosAprox() != null
                        && detalle.getPresentacion().getPesoGramosAprox() > 0
                        && detalle.getSabores() != null && !detalle.getSabores().isEmpty()) {
                    double kilosPorSabor = Math.round((((double) detalle.getPresentacion().getPesoGramosAprox() * detalle.getCantidad())
                            / (1000.0 * detalle.getSabores().size())) * 1000.0) / 1000.0;

                    for (Sabor sabor : detalle.getSabores()) {
                        if (sabor.getStockKilos() != null) {
                            double stockAnt = sabor.getStockKilos();
                            double nuevoStock = Math.round((sabor.getStockKilos() + kilosPorSabor) * 1000.0) / 1000.0;
                            sabor.setStockKilos(nuevoStock);
                            if (nuevoStock > 0.001 && !Boolean.TRUE.equals(sabor.getDisponible())) {
                                sabor.setDisponible(true);
                            }
                            saborRepository.save(sabor);
                            inventarioService.registrarMovimientoInterno(
                                    TipoItemStock.SABOR,
                                    sabor.getId(),
                                    sabor.getNombre(),
                                    TipoMovimientoStock.REINTEGRO_CANCELACION,
                                    kilosPorSabor,
                                    stockAnt,
                                    nuevoStock,
                                    detalleLog,
                                    usuarioCancelacion
                            );
                        }
                    }
                }
            }
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
        Pedido guardado = pedidoRepository.save(pedido);

        // Registrar evento en la bitácora de auditoría
        auditoriaService.registrarLog(
                usuarioCancelacion,
                "VENTAS",
                "CANCELACION",
                pedido.getId(),
                docRef,
                "Comprobante anulado. Total: Gs. " + pedido.getTotal() + ". Autorizado por: " + usuarioCancelacion + motivoStr,
                null
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public void imprimirTicket(Long pedidoId, Long impresoraId) {
        Pedido pedido = obtenerPorId(pedidoId);
        ticketPrinterService.imprimirPedido(impresoraId, pedido);
    }
}
