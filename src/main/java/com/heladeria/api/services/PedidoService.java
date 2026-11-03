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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    private final TrabajoImpresionService trabajoImpresionService;
    private final LimitadorIntentos limitadorIntentos;
    private final SesionCajaRepository sesionCajaRepository;
    private final InstitucionRepository institucionRepository;
    private final ClienteService clienteService;
    private final InventarioService inventarioService;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final PuntoExpedicionRepository puntoExpedicionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Autowired
    private StockMaestroService stockMaestroService;

    public PedidoService(PedidoRepository pedidoRepository,
                         PresentacionRepository presentacionRepository,
                         SaborRepository saborRepository,
                         ToppingRepository toppingRepository,
                         ProductoSimpleRepository productoSimpleRepository,
                         TicketPrinterService ticketPrinterService,
                         TrabajoImpresionService trabajoImpresionService,
                         LimitadorIntentos limitadorIntentos,
                         SesionCajaRepository sesionCajaRepository,
                         InstitucionRepository institucionRepository,
                         ClienteService clienteService,
                         InventarioService inventarioService,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         PasswordEncoder passwordEncoder,
                         AuditoriaService auditoriaService,
                         PuntoExpedicionRepository puntoExpedicionRepository) {
        this.pedidoRepository = pedidoRepository;
        this.presentacionRepository = presentacionRepository;
        this.saborRepository = saborRepository;
        this.toppingRepository = toppingRepository;
        this.productoSimpleRepository = productoSimpleRepository;
        this.ticketPrinterService = ticketPrinterService;
        this.trabajoImpresionService = trabajoImpresionService;
        this.limitadorIntentos = limitadorIntentos;
        this.sesionCajaRepository = sesionCajaRepository;
        this.institucionRepository = institucionRepository;
        this.clienteService = clienteService;
        this.inventarioService = inventarioService;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.puntoExpedicionRepository = puntoExpedicionRepository;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = com.heladeria.api.security.AuthUtils.requireUsername(auth);
        return crearPedido(request, username);
    }

    /** Recupera el pedido ya registrado con ese trxId (para reenvíos y carreras de doble envío). */
    @Transactional(readOnly = true)
    public Pedido recuperarPorReenvio(String trxId, String username) {
        Pedido existente = pedidoRepository.findByTrxId(trxId.trim())
                .orElseThrow(() -> new ReglaDeNegocioException("No se pudo registrar la venta: conflicto de datos."));
        return validarReenvio(existente, username);
    }

    private Pedido validarReenvio(Pedido existente, String username) {
        String duenio = existente.getSesionCaja() != null && existente.getSesionCaja().getUsuarioApertura() != null
                ? existente.getSesionCaja().getUsuarioApertura().getUsername() : null;
        if (duenio != null && !duenio.equals(username)) {
            throw new ReglaDeNegocioException("El identificador de transacción ya fue utilizado por otra venta.");
        }
        return existente;
    }

    /**
     * Toma el número de factura al final de la venta, con el punto bloqueado (FOR UPDATE).
     * Así el bloqueo dura solo el tramo final de la transacción y no todo el procesamiento de ítems;
     * la tabla de numeración sigue serializada y el número se revierte junto con la venta si falla.
     */
    private void asignarNumeroFactura(Pedido pedido, PuntoExpedicion punto) {
        // Bloqueo pesimista para concurrencia atómica (SELECT ... FOR UPDATE)
        PuntoExpedicion puntoLock = puntoExpedicionRepository.findByIdForUpdate(punto.getId())
                .orElseThrow(() -> new ReglaDeNegocioException("Punto de expedición no encontrado para emitir factura."));

        Institucion inst = institucionRepository.findById(1L).orElse(null);

        // Sincronizar compatibilidad con institucion si es 001-001 (punto principal)
        if (inst != null && "001".equals(puntoLock.getEstablecimiento()) && "001".equals(puntoLock.getPuntoEmision())) {
            if (inst.getTimbrado() != null) puntoLock.setTimbrado(inst.getTimbrado());
            if (inst.getTimbradoInicio() != null) puntoLock.setTimbradoInicio(inst.getTimbradoInicio());
            if (inst.getTimbradoVencimiento() != null) puntoLock.setTimbradoVencimiento(inst.getTimbradoVencimiento());
            if (inst.getNumeroFinFactura() != null) puntoLock.setNumeroFinFactura(inst.getNumeroFinFactura());
            if (inst.getUltimoNumeroFactura() != null && inst.getUltimoNumeroFactura() > puntoLock.getUltimoNumeroFactura()) {
                puntoLock.setUltimoNumeroFactura(inst.getUltimoNumeroFactura());
            }
        }

        // Validar Vencimiento del Timbrado (del punto o fallback a institución)
        LocalDate timbradoVencimiento = puntoLock.getTimbradoVencimiento();
        String timbrado = puntoLock.getTimbrado();
        if (timbrado == null || timbrado.isBlank()) {
            if (inst != null) {
                timbrado = inst.getTimbrado();
                timbradoVencimiento = inst.getTimbradoVencimiento();
            }
        }
        if (timbrado == null || timbrado.isBlank()) {
            throw new IllegalStateException("No hay un timbrado fiscal cargado. Cárguelo en Datos del negocio / Puntos de expedición antes de emitir facturas.");
        }
        if (timbradoVencimiento != null && timbradoVencimiento.isBefore(LocalDate.now())) {
            throw new IllegalStateException("El timbrado fiscal N° " + timbrado + " ha vencido el " + timbradoVencimiento + ". Debe renovar el timbrado para poder emitir facturas legales.");
        }

        long sig = (puntoLock.getUltimoNumeroFactura() != null ? puntoLock.getUltimoNumeroFactura() : 0L) + 1;

        // Validar Límite del Talonario
        if (puntoLock.getNumeroFinFactura() != null && sig > puntoLock.getNumeroFinFactura()) {
            throw new IllegalStateException("Se ha alcanzado el límite máximo del talonario (" + puntoLock.getNumeroFinFactura() + "). Debe habilitar un nuevo talonario desde el módulo contable.");
        }

        String estab = (puntoLock.getEstablecimiento() != null && !puntoLock.getEstablecimiento().isBlank()) ? puntoLock.getEstablecimiento() : "001";
        String pto = (puntoLock.getPuntoEmision() != null && !puntoLock.getPuntoEmision().isBlank()) ? puntoLock.getPuntoEmision() : "001";
        pedido.setNumeroFactura(String.format("%s-%s-%07d", estab, pto, sig));
        pedido.setTimbrado(timbrado);
        pedido.setTimbradoVencimiento(timbradoVencimiento);

        puntoLock.setUltimoNumeroFactura(sig);
        puntoExpedicionRepository.save(puntoLock);
        pedido.setPuntoExpedicion(puntoLock);

        // Sincronizar compatibilidad con institucion si es 001-001
        if ("001".equals(estab) && "001".equals(pto)) {
            institucionRepository.findById(1L).ifPresent(i -> {
                i.setUltimoNumeroFactura(sig);
                institucionRepository.save(i);
            });
        }
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request, String username) {
        if (username == null || username.isBlank()) {
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            username = com.heladeria.api.security.AuthUtils.requireUsername(auth);
        }

        // Idempotencia: si el POS reenvía la misma venta (doble clic, reintento por red lenta),
        // se devuelve la ya registrada en vez de cobrar de nuevo o fallar con un conflicto.
        if (request.getTrxId() != null && !request.getTrxId().isBlank()) {
            Pedido existente = pedidoRepository.findByTrxId(request.getTrxId().trim()).orElse(null);
            if (existente != null) {
                return validarReenvio(existente, username);
            }
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

        if ((request.getMetodoPago() == MetodoPago.TARJETA_DEBITO || request.getMetodoPago() == MetodoPago.TARJETA_CREDITO)
                && (request.getNumeroVoucher() == null || request.getNumeroVoucher().trim().isEmpty())) {
            throw new ReglaDeNegocioException("El número de voucher / boleta es obligatorio para pagos con tarjeta de débito o crédito.");
        }

        PuntoExpedicion puntoExpedicionAsignado = null;
        if (tipoComprobante == TipoComprobante.FACTURA) {
            if (request.getClienteRuc() == null || request.getClienteRuc().trim().isEmpty()
                    || request.getClienteNombre() == null || request.getClienteNombre().trim().isEmpty()) {
                throw new ReglaDeNegocioException("Para emitir factura legal debe indicar el RUC y la Razón Social del cliente.");
            }

            PuntoExpedicion punto = sesionActiva.getPuntoExpedicion();
            if (punto == null) {
                punto = puntoExpedicionRepository.findByActivoTrueOrderByEstablecimientoAscPuntoEmisionAsc().stream().findFirst().orElse(null);
            }
            if (punto == null) {
                // Auto-inicialización / migración desde Institucion si aún no existe en DB
                Institucion inst = institucionRepository.findById(1L).orElseGet(() -> {
                    Institucion nueva = new Institucion();
                    nueva.setId(1L);
                    nueva.setNombre("Heladería Artesanal");
                    return institucionRepository.save(nueva);
                });
                String estab = (inst.getEstablecimiento() != null && !inst.getEstablecimiento().isBlank()) ? inst.getEstablecimiento() : "001";
                String pto = (inst.getPuntoEmision() != null && !inst.getPuntoEmision().isBlank()) ? inst.getPuntoEmision() : "001";
                punto = PuntoExpedicion.builder()
                        .nombre("Caja 1 - Principal")
                        .establecimiento(estab)
                        .puntoEmision(pto)
                        .timbrado(inst.getTimbrado())
                        .timbradoInicio(inst.getTimbradoInicio())
                        .timbradoVencimiento(inst.getTimbradoVencimiento())
                        .numeroInicioFactura(inst.getNumeroInicioFactura() != null ? inst.getNumeroInicioFactura() : 1L)
                        .numeroFinFactura(inst.getNumeroFinFactura() != null ? inst.getNumeroFinFactura() : 1000L)
                        .ultimoNumeroFactura(inst.getUltimoNumeroFactura() != null ? inst.getUltimoNumeroFactura() : 0L)
                        .activo(true)
                        .build();
                punto = puntoExpedicionRepository.save(punto);
                sesionActiva.setPuntoExpedicion(punto);
                sesionCajaRepository.save(sesionActiva);
            }

            puntoExpedicionAsignado = punto;
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
                .puntoExpedicion(puntoExpedicionAsignado != null ? puntoExpedicionAsignado : sesionActiva.getPuntoExpedicion())
                .tipoComprobante(tipoComprobante)
                .condicionVenta(condicionVenta)
                .clienteNombre(clienteNombre)
                .clienteRuc(clienteRuc)
                .clienteDireccion(clienteDireccion)
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
                    int cant = itemDto.getCantidad();
                    int afectadas = presentacionRepository.descontarStock(presentacion.getId(), cant);
                    entityManager.refresh(presentacion);
                    if (afectadas == 0) {
                        throw new ReglaDeNegocioException("Stock insuficiente de envases para '" + presentacion.getNombre() +
                                "'. Disponible: " + presentacion.getStock() + ", Solicitado: " + cant);
                    }
                    stockMaestroService.espejarPresentacion(presentacion, BigDecimal.valueOf(-cant));
                    double stockPost = presentacion.getStock().doubleValue();
                    double stockAnt = stockPost + cant;
                    inventarioService.registrarMovimientoInterno(
                            TipoItemStock.ENVASE,
                            presentacion.getId(),
                            presentacion.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA,
                            itemDto.getCantidad().doubleValue(),
                            stockAnt,
                            stockPost,
                            "Venta POS #" + trxId,
                            username
                    );
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

                    // Descuento atómico por sabor, en orden de id para no provocar deadlocks entre ventas concurrentes.
                    // Si algún sabor no alcanza se lanza excepción y la transacción completa hace rollback.
                    List<Sabor> saboresOrdenados = new ArrayList<>(sabores);
                    saboresOrdenados.sort(java.util.Comparator.comparing(Sabor::getId));
                    for (Sabor sabor : saboresOrdenados) {
                        if (sabor.getStockKilos() != null) {
                            int afectadas = saborRepository.descontarStockKilos(sabor.getId(), kilosPorSabor);
                            entityManager.refresh(sabor);
                            if (afectadas == 0) {
                                throw new ReglaDeNegocioException(String.format(java.util.Locale.US,
                                        "Stock insuficiente de helado para el sabor '%s'. Disponible: %.2f kg, Solicitado: %.2f kg",
                                        sabor.getNombre(), sabor.getStockKilos(), kilosPorSabor));
                            }
                            stockMaestroService.espejarSabor(sabor, BigDecimal.valueOf(-kilosPorSabor));
                            double nuevoStock = sabor.getStockKilos();
                            double stockAnt = Math.round((nuevoStock + kilosPorSabor) * 1000.0) / 1000.0;
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
                                    "Venta POS #" + trxId,
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
                Optional<Producto> optCombo = resolverComboCompuesto(prod);

                if (optCombo.isPresent() && optCombo.get().getTipoArticulo() == TipoArticulo.COMBO
                        && optCombo.get().getComponentes() != null && !optCombo.get().getComponentes().isEmpty()) {
                    // Es un Combo Compuesto: Descontar stock de cada uno de sus componentes individuales
                    Producto combo = optCombo.get();
                    String conceptoCombo = "Venta Combo #" + trxId + " (" + prod.getNombre() + ")";
                    // Orden por id para que ventas concurrentes de combos que comparten componentes no se bloqueen entre sí
                    List<ComboComponente> componentes = new ArrayList<>(combo.getComponentes());
                    componentes.sort(java.util.Comparator.comparing(cc -> cc.getComponente().getId()));
                    for (ComboComponente cc : componentes) {
                        Producto comp = cc.getComponente();
                        BigDecimal cantFactor = cc.getCantidad() != null ? cc.getCantidad() : BigDecimal.ONE;
                        BigDecimal cantTotalDescontar = cantFactor.multiply(BigDecimal.valueOf(itemDto.getCantidad()));
                        descontarComponenteDeCombo(comp, cantTotalDescontar, prod.getNombre(), conceptoCombo, finalUsername);
                    }
                } else {
                    // Control de stock estandar (Combo simple o producto unitario)
                    if (prod.getStock() != null) {
                        int cant = itemDto.getCantidad();
                        int afectadas = productoSimpleRepository.descontarStock(prod.getId(), cant);
                        entityManager.refresh(prod);
                        if (afectadas == 0) {
                            throw new ReglaDeNegocioException("Stock insuficiente para '" + prod.getNombre() +
                                    "'. Disponible: " + prod.getStock() + ", Solicitado: " + cant);
                        }
                        stockMaestroService.espejarProductoSimple(prod, BigDecimal.valueOf(-cant));
                        double stockPost = prod.getStock().doubleValue();
                        double stockAnt = stockPost + cant;
                        inventarioService.registrarMovimientoInterno(
                                TipoItemStock.PRODUCTO,
                                prod.getId(),
                                prod.getNombre(),
                                TipoMovimientoStock.SALIDA_VENTA,
                                itemDto.getCantidad().doubleValue(),
                                stockAnt,
                                stockPost,
                                "Venta POS #" + trxId,
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
        if (tipoComprobante == TipoComprobante.FACTURA) {
            asignarNumeroFactura(pedido, puntoExpedicionAsignado);
        }
        Pedido guardado = pedidoRepository.save(pedido);

        if (request.getImpresoraId() != null) {
            // Outbox: el trabajo se guarda en esta misma transacción y se imprime recién después del commit.
            trabajoImpresionService.encolarTicketPedido(guardado.getId(), request.getImpresoraId(), username);
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

    // ---------------------------------------------------------------------------------------------
    // Componentes de combos: descuento atómico con validación de stock (misma garantía que la venta directa)
    // ---------------------------------------------------------------------------------------------

    /** Devuelve el maestro del producto si es un combo compuesto (con componentes registrados). */
    private Optional<Producto> resolverComboCompuesto(ProductoSimple prod) {
        Optional<Producto> opt = Optional.empty();
        if (prod.getCodigo() != null && !prod.getCodigo().isBlank()) {
            opt = productoRepository.findBySkuIgnoreCase(prod.getCodigo().trim());
        }
        if (opt.isEmpty() && prod.getCodigoBarra() != null && !prod.getCodigoBarra().isBlank()) {
            opt = productoRepository.findByCodigoBarraIgnoreCase(prod.getCodigoBarra().trim());
        }
        if (opt.isEmpty()) {
            opt = productoRepository.findByNombreIgnoreCase(prod.getNombre().trim());
        }
        return opt.filter(c -> c.getTipoArticulo() == TipoArticulo.COMBO
                && c.getComponentes() != null && !c.getComponentes().isEmpty());
    }

    /** Inverso de {@link #descontarComponenteDeCombo}: devuelve el stock de un componente al anular la venta. */
    private void reintegrarComponenteDeCombo(Producto comp, BigDecimal cantidad, String concepto, String usuario) {
        if (comp.getStock() != null) {
            productoRepository.aumentarStock(comp.getId(), cantidad);
            entityManager.refresh(comp);
        }

        String nombre = comp.getNombre().trim();
        if (comp.getTipoArticulo() == TipoArticulo.ENVASE) {
            presentacionRepository.findByNombreIgnoreCase(nombre).ifPresent(pres -> {
                if (pres.getStock() != null) {
                    int cant = (int) Math.round(cantidad.doubleValue());
                    presentacionRepository.aumentarStock(pres.getId(), cant);
                    entityManager.refresh(pres);
                    double post = pres.getStock().doubleValue();
                    inventarioService.registrarMovimientoInterno(TipoItemStock.ENVASE, pres.getId(), pres.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION, cantidad.doubleValue(), post - cant, post, concepto, usuario);
                }
            });
        } else if (comp.getTipoArticulo() == TipoArticulo.TOPPING) {
            toppingRepository.findByNombreIgnoreCase(nombre).ifPresent(top ->
                    inventarioService.registrarMovimientoInterno(TipoItemStock.PRODUCTO, top.getId(), top.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION, cantidad.doubleValue(), 0.0, 0.0, concepto, usuario));
        } else if (comp.getTipoArticulo() == TipoArticulo.SABOR) {
            saborRepository.findByNombreIgnoreCase(nombre).ifPresent(sab -> {
                if (sab.getStockKilos() != null) {
                    saborRepository.aumentarStockKilos(sab.getId(), cantidad.doubleValue());
                    entityManager.refresh(sab);
                    double post = sab.getStockKilos();
                    if (post > 0.001 && !Boolean.TRUE.equals(sab.getDisponible())) {
                        sab.setDisponible(true);
                        saborRepository.save(sab);
                    }
                    inventarioService.registrarMovimientoInterno(TipoItemStock.SABOR, sab.getId(), sab.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION, cantidad.doubleValue(), post - cantidad.doubleValue(), post, concepto, usuario);
                }
            });
        } else {
            productoSimpleRepository.findByNombreIgnoreCase(nombre).ifPresent(ps -> {
                if (ps.getStock() != null) {
                    int cant = (int) Math.round(cantidad.doubleValue());
                    productoSimpleRepository.aumentarStock(ps.getId(), cant);
                    entityManager.refresh(ps);
                    double post = ps.getStock().doubleValue();
                    inventarioService.registrarMovimientoInterno(TipoItemStock.PRODUCTO, ps.getId(), ps.getNombre(),
                            TipoMovimientoStock.REINTEGRO_CANCELACION, cantidad.doubleValue(), post - cant, post, concepto, usuario);
                }
            });
        }
    }

    private void descontarComponenteDeCombo(Producto comp, BigDecimal cantidad, String nombreCombo, String concepto, String usuario) {
        // 1) Maestro de productos
        if (comp.getStock() != null) {
            int afectadas = productoRepository.descontarStock(comp.getId(), cantidad);
            entityManager.refresh(comp);
            if (afectadas == 0) {
                throw new ReglaDeNegocioException("Stock insuficiente del componente '" + comp.getNombre() + "' del combo '" + nombreCombo
                        + "'. Disponible: " + comp.getStock() + ", Requerido: " + cantidad);
            }
        }

        // 2) Catálogo operativo según el tipo de componente
        String nombre = comp.getNombre().trim();
        if (comp.getTipoArticulo() == TipoArticulo.ENVASE) {
            presentacionRepository.findByNombreIgnoreCase(nombre).ifPresent(pres -> {
                if (pres.getStock() != null) {
                    int cant = (int) Math.round(cantidad.doubleValue());
                    int afectadas = presentacionRepository.descontarStock(pres.getId(), cant);
                    entityManager.refresh(pres);
                    if (afectadas == 0) {
                        throw new ReglaDeNegocioException("Stock insuficiente de envases '" + pres.getNombre() + "' para el combo '" + nombreCombo
                                + "'. Disponible: " + pres.getStock() + ", Requerido: " + cant);
                    }
                    double post = pres.getStock().doubleValue();
                    inventarioService.registrarMovimientoInterno(TipoItemStock.ENVASE, pres.getId(), pres.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA, cantidad.doubleValue(), post + cant, post, concepto, usuario);
                }
            });
        } else if (comp.getTipoArticulo() == TipoArticulo.TOPPING) {
            toppingRepository.findByNombreIgnoreCase(nombre).ifPresent(top ->
                    inventarioService.registrarMovimientoInterno(TipoItemStock.PRODUCTO, top.getId(), top.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA, cantidad.doubleValue(), 0.0, 0.0, concepto, usuario));
        } else if (comp.getTipoArticulo() == TipoArticulo.SABOR) {
            saborRepository.findByNombreIgnoreCase(nombre).ifPresent(sab -> {
                if (sab.getStockKilos() != null) {
                    int afectadas = saborRepository.descontarStockKilos(sab.getId(), cantidad.doubleValue());
                    entityManager.refresh(sab);
                    if (afectadas == 0) {
                        throw new ReglaDeNegocioException("Stock insuficiente del sabor '" + sab.getNombre() + "' para el combo '" + nombreCombo
                                + "'. Disponible: " + sab.getStockKilos() + " kg, Requerido: " + cantidad + " kg");
                    }
                    double post = sab.getStockKilos();
                    if (post <= 0.001) {
                        sab.setDisponible(false);
                        saborRepository.save(sab);
                    }
                    inventarioService.registrarMovimientoInterno(TipoItemStock.SABOR, sab.getId(), sab.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA, cantidad.doubleValue(), post + cantidad.doubleValue(), post, concepto, usuario);
                }
            });
        } else {
            // VENTA_DIRECTA, INSUMO, ELABORADO
            productoSimpleRepository.findByNombreIgnoreCase(nombre).ifPresent(ps -> {
                if (ps.getStock() != null) {
                    int cant = (int) Math.round(cantidad.doubleValue());
                    int afectadas = productoSimpleRepository.descontarStock(ps.getId(), cant);
                    entityManager.refresh(ps);
                    if (afectadas == 0) {
                        throw new ReglaDeNegocioException("Stock insuficiente del producto '" + ps.getNombre() + "' para el combo '" + nombreCombo
                                + "'. Disponible: " + ps.getStock() + ", Requerido: " + cant);
                    }
                    double post = ps.getStock().doubleValue();
                    inventarioService.registrarMovimientoInterno(TipoItemStock.PRODUCTO, ps.getId(), ps.getNombre(),
                            TipoMovimientoStock.SALIDA_VENTA, cantidad.doubleValue(), post + cant, post, concepto, usuario);
                }
            });
        }
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
            // Límite de intentos por quien opera (no por el admin objetivo): evita adivinar claves de supervisor
            String quienIntenta = authUser != null && authUser.getName() != null ? authUser.getName() : "anonimo";
            String claveLimite = "anular:" + quienIntenta;
            limitadorIntentos.verificarNoBloqueado(claveLimite);

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
                boolean bloqueado = limitadorIntentos.registrarFallo(claveLimite);
                auditoriaService.registrarLog(quienIntenta, "VENTAS", "ALERTA", pedido.getId(), "Pedido #" + pedido.getId(),
                        "Clave de supervisor incorrecta al intentar anular el pedido" + (bloqueado ? " (usuario bloqueado temporalmente)" : "") + ".", null);
                throw new ReglaDeNegocioException("Contraseña de Administrador incorrecta. Operación de cancelación rechazada.");
            }
            limitadorIntentos.registrarExito(claveLimite);

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
            reintegrarStockDetalle(detalle, detalle.getCantidad(), detalleLog, usuarioCancelacion);
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

    /**
     * Devuelve al inventario la cantidad indicada de una línea de venta (anulación total o devolución parcial
     * con nota de crédito). Un combo compuesto devuelve sus componentes; un helado, el envase y los kilos de sabor.
     */
    public void reintegrarStockDetalle(DetallePedido detalle, int cantidad, String detalleLog, String usuarioCancelacion) {
        Optional<Producto> comboCompuesto = (detalle.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && detalle.getProductoSimple() != null)
                ? resolverComboCompuesto(detalle.getProductoSimple()) : Optional.empty();
        if (comboCompuesto.isPresent()) {
            // Combo compuesto: la venta descontó cada componente, así que se devuelven los componentes
            List<ComboComponente> componentes = new ArrayList<>(comboCompuesto.get().getComponentes());
            componentes.sort(java.util.Comparator.comparing(cc -> cc.getComponente().getId()));
            for (ComboComponente cc : componentes) {
                BigDecimal factor = cc.getCantidad() != null ? cc.getCantidad() : BigDecimal.ONE;
                reintegrarComponenteDeCombo(cc.getComponente(), factor.multiply(BigDecimal.valueOf(cantidad)),
                        detalleLog, usuarioCancelacion);
            }
        } else if (detalle.getTipoItem() == TipoItemPedido.PRODUCTO_SIMPLE && detalle.getProductoSimple() != null) {
            ProductoSimple prod = detalle.getProductoSimple();
            if (prod.getStock() != null) {
                productoSimpleRepository.aumentarStock(prod.getId(), cantidad);
                entityManager.refresh(prod);
                stockMaestroService.espejarProductoSimple(prod, BigDecimal.valueOf(cantidad));
                double stockPost = prod.getStock().doubleValue();
                double stockAnt = stockPost - cantidad;
                inventarioService.registrarMovimientoInterno(
                        TipoItemStock.PRODUCTO,
                        prod.getId(),
                        prod.getNombre(),
                        TipoMovimientoStock.REINTEGRO_CANCELACION,
                        (double) cantidad,
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
                presentacionRepository.aumentarStock(pres.getId(), cantidad);
                entityManager.refresh(pres);
                stockMaestroService.espejarPresentacion(pres, BigDecimal.valueOf(cantidad));
                double stockPost = pres.getStock().doubleValue();
                double stockAnt = stockPost - cantidad;
                inventarioService.registrarMovimientoInterno(
                        TipoItemStock.ENVASE,
                        pres.getId(),
                        pres.getNombre(),
                        TipoMovimientoStock.REINTEGRO_CANCELACION,
                        (double) cantidad,
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
                double kilosPorSabor = Math.round((((double) detalle.getPresentacion().getPesoGramosAprox() * cantidad)
                        / (1000.0 * detalle.getSabores().size())) * 1000.0) / 1000.0;

                for (Sabor sabor : detalle.getSabores()) {
                    if (sabor.getStockKilos() != null) {
                        saborRepository.aumentarStockKilos(sabor.getId(), kilosPorSabor);
                        entityManager.refresh(sabor);
                        stockMaestroService.espejarSabor(sabor, BigDecimal.valueOf(kilosPorSabor));
                        double nuevoStock = Math.round(sabor.getStockKilos() * 1000.0) / 1000.0;
                        double stockAnt = Math.round((nuevoStock - kilosPorSabor) * 1000.0) / 1000.0;
                        if (nuevoStock > 0.001 && !Boolean.TRUE.equals(sabor.getDisponible())) {
                            sabor.setDisponible(true);
                            saborRepository.save(sabor);
                        }
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

    @Transactional(readOnly = true)
    public void imprimirTicket(Long pedidoId, Long impresoraId) {
        Pedido pedido = obtenerPorId(pedidoId);
        ticketPrinterService.imprimirPedido(impresoraId, pedido);
    }
}
