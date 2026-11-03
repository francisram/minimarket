package com.heladeria.api.services;

import com.heladeria.api.dto.*;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.util.MonedaPyUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Notas de crédito: anulan total o parcialmente una factura ya emitida, con su propio timbrado y numeración.
 * Reintegran el stock de lo devuelto, devuelven el dinero de la caja (ventas en efectivo) y entran al libro IVA
 * con signo negativo.
 */
@Service
public class NotaCreditoService {

    private final NotaCreditoRepository notaCreditoRepository;
    private final SerieNotaCreditoRepository serieRepository;
    private final PedidoRepository pedidoRepository;
    private final PuntoExpedicionRepository puntoExpedicionRepository;
    private final SesionCajaRepository sesionCajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PedidoService pedidoService;
    private final CajaService cajaService;
    private final AuditoriaService auditoriaService;
    private final LimitadorIntentos limitadorIntentos;

    public NotaCreditoService(NotaCreditoRepository notaCreditoRepository, SerieNotaCreditoRepository serieRepository,
                              PedidoRepository pedidoRepository, PuntoExpedicionRepository puntoExpedicionRepository,
                              SesionCajaRepository sesionCajaRepository, UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder, PedidoService pedidoService, CajaService cajaService,
                              AuditoriaService auditoriaService, LimitadorIntentos limitadorIntentos) {
        this.notaCreditoRepository = notaCreditoRepository;
        this.serieRepository = serieRepository;
        this.pedidoRepository = pedidoRepository;
        this.puntoExpedicionRepository = puntoExpedicionRepository;
        this.sesionCajaRepository = sesionCajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.pedidoService = pedidoService;
        this.cajaService = cajaService;
        this.auditoriaService = auditoriaService;
        this.limitadorIntentos = limitadorIntentos;
    }

    // ---------------------------------------------------------------- consulta

    @Transactional(readOnly = true)
    public FacturaParaNotaCreditoDTO buscarFactura(String numeroFactura) {
        Pedido pedido = pedidoRepository.findByNumeroFacturaOrderByFechaCreacionDesc(numeroFactura.trim()).stream()
                .filter(p -> p.getTipoComprobante() == TipoComprobante.FACTURA)
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una factura con el número " + numeroFactura.trim() + "."));
        return facturaParaNotaCredito(pedido);
    }

    @Transactional(readOnly = true)
    public FacturaParaNotaCreditoDTO facturaDePedido(Long pedidoId) {
        return facturaParaNotaCredito(pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + pedidoId + " no encontrado.")));
    }

    private FacturaParaNotaCreditoDTO facturaParaNotaCredito(Pedido p) {
        if (p.getTipoComprobante() != TipoComprobante.FACTURA) {
            throw new ReglaDeNegocioException("Solo se emiten notas de crédito sobre facturas.");
        }
        FacturaParaNotaCreditoDTO d = new FacturaParaNotaCreditoDTO();
        d.pedidoId = p.getId();
        d.numeroFactura = p.getNumeroFactura();
        d.timbrado = p.getTimbrado();
        d.fecha = p.getFechaCreacion();
        d.clienteNombre = p.getClienteNombre();
        d.clienteRuc = p.getClienteRuc();
        d.metodoPago = p.getMetodoPago() != null ? p.getMetodoPago().name() : null;
        d.estado = p.getEstado().name();
        d.total = p.getTotal();
        d.puntoExpedicionId = p.getPuntoExpedicion() != null ? p.getPuntoExpedicion().getId() : null;
        if (d.puntoExpedicionId != null) {
            var serie = serieRepository.findById(d.puntoExpedicionId).filter(s -> s.getTimbrado() != null && !s.getTimbrado().isBlank());
            d.serieConfigurada = serie.isPresent();
            d.serieVencimiento = serie.map(SerieNotaCredito::getTimbradoVencimiento).orElse(null);
        }
        d.lineas = p.getDetalles().stream().map(det -> {
            long acreditada = notaCreditoRepository.cantidadAcreditada(det.getId());
            return new FacturaParaNotaCreditoDTO.Linea(det.getId(), descripcion(det), det.getCantidad(), acreditada,
                    det.getCantidad() - acreditada, det.getPrecioUnitario(), det.getSubtotal(),
                    det.getTipoIva() != null ? det.getTipoIva().name() : TipoIva.IVA_10.name());
        }).toList();
        d.notasCredito = notaCreditoRepository.findByPedido_IdOrderByFechaAsc(p.getId()).stream().map(NotaCreditoDTO::desde).toList();
        return d;
    }

    @Transactional(readOnly = true)
    public NotaCreditoDTO obtener(Long id) {
        return NotaCreditoDTO.desde(notaCreditoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Nota de crédito " + id + " no encontrada.")));
    }

    @Transactional(readOnly = true)
    public Optional<SerieNotaCreditoDTO> serie(Long puntoId) {
        return serieRepository.findById(puntoId).map(SerieNotaCreditoDTO::desde);
    }

    @Transactional
    public SerieNotaCreditoDTO guardarSerie(Long puntoId, SerieNotaCreditoDTO dto) {
        puntoExpedicionRepository.findById(puntoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de expedición " + puntoId + " no encontrado."));
        if (dto.numeroInicio != null && dto.numeroFin != null && dto.numeroInicio > dto.numeroFin) {
            throw new ReglaDeNegocioException("El número de inicio no puede ser mayor que el número fin.");
        }
        SerieNotaCredito s = serieRepository.findById(puntoId).orElseGet(() -> new SerieNotaCredito(puntoId));
        s.setTimbrado(dto.timbrado.trim());
        s.setTimbradoInicio(dto.timbradoInicio);
        s.setTimbradoVencimiento(dto.timbradoVencimiento);
        s.setNumeroInicio(dto.numeroInicio);
        s.setNumeroFin(dto.numeroFin);
        if (dto.ultimoNumero != null) {
            s.setUltimoNumero(dto.ultimoNumero);
        } else if (dto.numeroInicio != null && s.getUltimoNumero() < dto.numeroInicio - 1) {
            s.setUltimoNumero(dto.numeroInicio - 1);
        }
        s = serieRepository.save(s);
        auditoriaService.registrar("NOTAS_CREDITO", "SERIE", puntoId, "Punto " + puntoId,
                "Serie de notas de crédito: timbrado " + s.getTimbrado() + ", vence " + s.getTimbradoVencimiento());
        return SerieNotaCreditoDTO.desde(s);
    }

    // ---------------------------------------------------------------- emisión

    @Transactional
    public NotaCreditoDTO emitir(Long pedidoId, NotaCreditoRequestDTO req, String username) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido " + pedidoId + " no encontrado."));
        if (pedido.getTipoComprobante() != TipoComprobante.FACTURA || pedido.getNumeroFactura() == null) {
            throw new ReglaDeNegocioException("Solo se emiten notas de crédito sobre facturas.");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ReglaDeNegocioException("La factura está anulada: no admite notas de crédito.");
        }
        String autorizadoPor = autorizar(req, username, pedido);

        // Líneas a acreditar (sin líneas = toda la factura)
        Map<Long, DetallePedido> porId = new HashMap<>();
        pedido.getDetalles().forEach(d -> porId.put(d.getId(), d));
        Map<Long, Integer> pedidas = new LinkedHashMap<>();
        if (req.getItems() == null || req.getItems().isEmpty()) {
            for (DetallePedido d : pedido.getDetalles()) {
                long disp = d.getCantidad() - notaCreditoRepository.cantidadAcreditada(d.getId());
                if (disp > 0) pedidas.put(d.getId(), (int) disp);
            }
        } else {
            for (NotaCreditoRequestDTO.Linea l : req.getItems()) {
                if (!porId.containsKey(l.getDetalleId())) {
                    throw new ReglaDeNegocioException("La línea " + l.getDetalleId() + " no pertenece a esta factura.");
                }
                pedidas.merge(l.getDetalleId(), l.getCantidad(), Integer::sum);
            }
        }
        if (pedidas.isEmpty()) {
            throw new ReglaDeNegocioException("La factura ya fue acreditada por completo.");
        }

        NotaCredito nota = new NotaCredito();
        BigDecimal acum10 = BigDecimal.ZERO, acum5 = BigDecimal.ZERO, exentas = BigDecimal.ZERO;
        for (var e : pedidas.entrySet()) {
            DetallePedido det = porId.get(e.getKey());
            int cant = e.getValue();
            long yaAcreditada = notaCreditoRepository.cantidadAcreditada(det.getId());
            long disponible = det.getCantidad() - yaAcreditada;
            if (cant > disponible) {
                throw new ReglaDeNegocioException("No se puede acreditar " + cant + " de '" + descripcion(det)
                        + "': solo quedan " + disponible + " por acreditar.");
            }
            // La última porción de una línea toma lo que resta, para que la suma de las notas sea exactamente el subtotal
            BigDecimal monto = (cant == disponible)
                    ? det.getSubtotal().subtract(notaCreditoRepository.montoAcreditado(det.getId()))
                    : MonedaPyUtils.redondearGs(det.getSubtotal().multiply(BigDecimal.valueOf(cant))
                            .divide(BigDecimal.valueOf(det.getCantidad()), 4, RoundingMode.HALF_UP));
            TipoIva iva = det.getTipoIva() != null ? det.getTipoIva() : TipoIva.IVA_10;
            switch (iva) {
                case IVA_10 -> acum10 = acum10.add(monto);
                case IVA_5 -> acum5 = acum5.add(monto);
                default -> exentas = exentas.add(monto);
            }
            DetalleNotaCredito dn = new DetalleNotaCredito();
            dn.setDetallePedido(det);
            dn.setDescripcion(descripcion(det));
            dn.setCantidad(cant);
            dn.setPrecioUnitario(MonedaPyUtils.redondearGs(monto.divide(BigDecimal.valueOf(cant), 4, RoundingMode.HALF_UP)));
            dn.setSubtotal(monto);
            dn.setTipoIva(iva);
            nota.agregarDetalle(dn);
        }

        nota.setTotalExentas(exentas);
        nota.setTotalGravada10(MonedaPyUtils.calcularGravada10(acum10));
        nota.setTotalIva10(MonedaPyUtils.calcularIva10(acum10));
        nota.setTotalGravada5(MonedaPyUtils.calcularGravada5(acum5));
        nota.setTotalIva5(MonedaPyUtils.calcularIva5(acum5));
        nota.setTotalIva(MonedaPyUtils.redondearGs(nota.getTotalIva5().add(nota.getTotalIva10())));
        nota.setTotal(exentas.add(acum5).add(acum10));

        // Numeración propia, tomada con la fila de la serie bloqueada
        PuntoExpedicion punto = pedido.getPuntoExpedicion();
        if (punto == null) {
            throw new ReglaDeNegocioException("La factura no tiene punto de expedición: no se puede numerar la nota de crédito.");
        }
        SerieNotaCredito serie = serieRepository.findByIdForUpdate(punto.getId()).orElse(null);
        if (serie == null || serie.getTimbrado() == null || serie.getTimbrado().isBlank()) {
            throw new ReglaDeNegocioException("No hay un timbrado de notas de crédito cargado para el punto "
                    + punto.getEstablecimiento() + "-" + punto.getPuntoEmision() + ". Cárguelo antes de emitir.");
        }
        if (serie.getTimbradoVencimiento() != null && serie.getTimbradoVencimiento().isBefore(LocalDate.now())) {
            throw new ReglaDeNegocioException("El timbrado de notas de crédito N° " + serie.getTimbrado() + " venció el "
                    + serie.getTimbradoVencimiento() + ". Debe renovarlo.");
        }
        long siguiente = serie.getUltimoNumero() + 1;
        if (serie.getNumeroInicio() != null && siguiente < serie.getNumeroInicio()) {
            siguiente = serie.getNumeroInicio();
        }
        if (serie.getNumeroFin() != null && siguiente > serie.getNumeroFin()) {
            throw new ReglaDeNegocioException("Se alcanzó el límite del talonario de notas de crédito (" + serie.getNumeroFin() + ").");
        }
        serie.setUltimoNumero(siguiente);
        serieRepository.save(serie);

        nota.setNumero(String.format("%s-%s-%07d", punto.getEstablecimiento(), punto.getPuntoEmision(), siguiente));
        nota.setTimbrado(serie.getTimbrado());
        nota.setTimbradoVencimiento(serie.getTimbradoVencimiento());
        nota.setPuntoExpedicion(punto);
        nota.setPedido(pedido);
        nota.setFecha(LocalDateTime.now());
        nota.setMotivo(req.getMotivo().trim());
        nota.setReintegraStock(req.isReintegrarStock());
        nota.setUsuario(username);
        nota.setAutorizadoPor(autorizadoPor);
        nota = notaCreditoRepository.save(nota);

        String concepto = "Devolución NC " + nota.getNumero() + " (factura " + pedido.getNumeroFactura() + ")";
        if (req.isReintegrarStock()) {
            for (var e : pedidas.entrySet()) {
                pedidoService.reintegrarStockDetalle(porId.get(e.getKey()), e.getValue(), concepto, username);
            }
        }

        NotaCreditoDTO dto = NotaCreditoDTO.desde(nota);
        dto.egresoCajaRegistrado = devolverEfectivo(pedido, nota, username);

        auditoriaService.registrarLog(username, "NOTAS_CREDITO", "EMISION", nota.getId(), nota.getNumero(),
                "Nota de crédito " + nota.getNumero() + " sobre factura " + pedido.getNumeroFactura() + " por Gs. " + nota.getTotal()
                        + ". Motivo: " + nota.getMotivo() + ". Autorizado por: " + autorizadoPor
                        + (req.isReintegrarStock() ? "" : " (sin reintegro de stock)"), null);
        return dto;
    }

    /** Si la venta fue en efectivo y quien emite tiene caja abierta, el dinero devuelto sale de esa caja. */
    private boolean devolverEfectivo(Pedido pedido, NotaCredito nota, String username) {
        if (pedido.getMetodoPago() != MetodoPago.EFECTIVO) {
            return false;
        }
        if (sesionCajaRepository.findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(username, EstadoSesionCaja.ABIERTA).isEmpty()) {
            return false;
        }
        cajaService.registrarMovimiento(new RegistrarMovimientoCajaRequestDTO(null, TipoMovimientoCaja.EGRESO,
                CategoriaMovimientoCaja.DEVOLUCION_CLIENTE, "Devolución al cliente por NC " + nota.getNumero(),
                nota.getNumero(), nota.getTotal()), username);
        return true;
    }

    /** Quien emite debe ser administrador/supervisor, o presentar la clave de un administrador. Devuelve quién autorizó. */
    private String autorizar(NotaCreditoRequestDTO req, String username, Pedido pedido) {
        if (cajaService.tieneRolPrivilegiado(username)) {
            return username;
        }
        if (req.getAdminPassword() == null || req.getAdminPassword().isBlank()) {
            throw new ReglaDeNegocioException("Se requiere la contraseña de un Administrador o Propietario para emitir una nota de crédito.");
        }
        String claveLimite = "nc:" + username;
        limitadorIntentos.verificarNoBloqueado(claveLimite);
        Usuario admin = usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .filter(u -> u.getRol() != null && ("ADMIN".equalsIgnoreCase(u.getRol().getNombreRol()) || "OWNER".equalsIgnoreCase(u.getRol().getNombreRol())))
                .filter(u -> passwordEncoder.matches(req.getAdminPassword(), u.getPasswordHash()))
                .findFirst().orElse(null);
        if (admin == null) {
            boolean bloqueado = limitadorIntentos.registrarFallo(claveLimite);
            auditoriaService.registrarLog(username, "NOTAS_CREDITO", "ALERTA", pedido.getId(), pedido.getNumeroFactura(),
                    "Clave de administrador incorrecta al intentar emitir una nota de crédito" + (bloqueado ? " (usuario bloqueado temporalmente)" : "") + ".", null);
            throw new ReglaDeNegocioException("Contraseña de Administrador incorrecta. Nota de crédito rechazada.");
        }
        limitadorIntentos.registrarExito(claveLimite);
        return admin.getUsername();
    }

    private static String descripcion(DetallePedido d) {
        String base = switch (d.getTipoItem()) {
            case HELADO -> d.getPresentacion() != null ? d.getPresentacion().getNombre() : "Helado";
            case PRODUCTO_SIMPLE -> d.getProductoSimple() != null ? d.getProductoSimple().getNombre() : "Producto";
            default -> "Topping";
        };
        if (d.getTipoItem() == TipoItemPedido.HELADO && d.getSabores() != null && !d.getSabores().isEmpty()) {
            base += " (" + String.join(", ", d.getSabores().stream().map(Sabor::getNombre).toList()) + ")";
        }
        return base.length() > 200 ? base.substring(0, 200) : base;
    }

    // ---------------------------------------------------------------- libro IVA

    @Transactional(readOnly = true)
    public List<NotaCredito> delPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return notaCreditoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }
}
