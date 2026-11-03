package com.heladeria.api.services;

import com.heladeria.api.dto.*;
import com.heladeria.api.entities.MovimientoCaja;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.PuntoExpedicion;
import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoMovimientoCaja;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.MovimientoCajaRepository;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.PuntoExpedicionRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.util.MonedaPyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final PuntoExpedicionRepository puntoExpedicionRepository;
    private final AuditoriaService auditoriaService;

    public CajaService(SesionCajaRepository sesionCajaRepository,
                       PedidoRepository pedidoRepository,
                       UsuarioRepository usuarioRepository,
                       MovimientoCajaRepository movimientoCajaRepository,
                       PuntoExpedicionRepository puntoExpedicionRepository,
                       AuditoriaService auditoriaService) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.puntoExpedicionRepository = puntoExpedicionRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public EstadoCajaDTO obtenerEstado() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : null;
        return obtenerEstado(username);
    }

    @Transactional(readOnly = true)
    public EstadoCajaDTO obtenerEstado(String username) {
        if (username == null || username.isBlank()) {
            return sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                    .map(s -> new EstadoCajaDTO(
                            true,
                            s.getId(),
                            s.getUsuarioApertura() != null ? s.getUsuarioApertura().getUsername() : null,
                            s.getFechaApertura(),
                            s.getMontoInicial(),
                            PuntoExpedicionDTO.fromEntity(s.getPuntoExpedicion())
                    ))
                    .orElseGet(() -> new EstadoCajaDTO(false, null, null, null, null));
        }

        return sesionCajaRepository.findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(username, EstadoSesionCaja.ABIERTA)
                .map(s -> new EstadoCajaDTO(
                        true,
                        s.getId(),
                        s.getUsuarioApertura() != null ? s.getUsuarioApertura().getUsername() : null,
                        s.getFechaApertura(),
                        s.getMontoInicial(),
                        PuntoExpedicionDTO.fromEntity(s.getPuntoExpedicion())
                ))
                .orElseGet(() -> new EstadoCajaDTO(false, null, null, null, null));
    }

    @Transactional(readOnly = true)
    public ResumenCajaDTO obtenerResumenActual() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.getName() != null) ? auth.getName() : null;
        return obtenerResumen(null, username);
    }

    @Transactional(readOnly = true)
    public ResumenCajaDTO obtenerResumen(Long sesionId, String username) {
        SesionCaja sesion;
        if (sesionId != null) {
            sesion = sesionCajaRepository.findById(sesionId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + sesionId + " no encontrada."));
            if (username != null) {
                verificarAccesoSesion(sesion, username);
            }
        } else {
            if (username != null && !username.isBlank()) {
                sesion = sesionCajaRepository.findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(username, EstadoSesionCaja.ABIERTA)
                        .orElseThrow(() -> new ReglaDeNegocioException("No existe una sesión de caja abierta para el usuario '" + username + "'."));
            } else {
                sesion = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                        .orElseThrow(() -> new ReglaDeNegocioException("No existe una sesión de caja abierta actualmente."));
            }
        }

        List<Pedido> pedidos = pedidoRepository.findBySesionCaja_IdAndEstadoNot(sesion.getId(), EstadoPedido.CANCELADO);

        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjetaDebito = BigDecimal.ZERO;
        BigDecimal totalTarjetaCredito = BigDecimal.ZERO;
        BigDecimal totalTransferenciaQr = BigDecimal.ZERO;
        BigDecimal totalVentas = BigDecimal.ZERO;
        int cantidadVouchers = 0;
        int cantidadTransferencias = 0;

        for (Pedido p : pedidos) {
            BigDecimal totalPedido = MonedaPyUtils.redondearGs(p.getTotal());
            totalVentas = totalVentas.add(totalPedido);

            if (p.getMetodoPago() == MetodoPago.EFECTIVO) {
                totalEfectivo = totalEfectivo.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                totalTarjetaDebito = totalTarjetaDebito.add(totalPedido);
                cantidadVouchers++;
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_CREDITO) {
                totalTarjetaCredito = totalTarjetaCredito.add(totalPedido);
                cantidadVouchers++;
            } else if (p.getMetodoPago() == MetodoPago.TRANSFERENCIA_QR) {
                totalTransferenciaQr = totalTransferenciaQr.add(totalPedido);
                cantidadTransferencias++;
            }
        }

        BigDecimal totalEsperadoTarjeta = MonedaPyUtils.redondearGs(totalTarjetaDebito.add(totalTarjetaCredito));

        List<MovimientoCaja> movimientos = movimientoCajaRepository.findBySesionCaja_IdOrderByFechaDesc(sesion.getId());
        BigDecimal totalIngresos = BigDecimal.ZERO;
        BigDecimal totalEgresos = BigDecimal.ZERO;
        for (MovimientoCaja m : movimientos) {
            if (m.getTipo() == TipoMovimientoCaja.INGRESO) {
                totalIngresos = totalIngresos.add(m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO);
            } else if (m.getTipo() == TipoMovimientoCaja.EGRESO) {
                totalEgresos = totalEgresos.add(m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO);
            }
        }
        totalIngresos = MonedaPyUtils.redondearGs(totalIngresos);
        totalEgresos = MonedaPyUtils.redondearGs(totalEgresos);

        BigDecimal totalEsperadoEfectivo = MonedaPyUtils.redondearGs(
                sesion.getMontoInicial().add(totalEfectivo).add(totalIngresos).subtract(totalEgresos)
        );

        List<MovimientoCajaDTO> movimientosDTO = movimientos.stream()
                .map(MovimientoCajaDTO::fromEntity)
                .collect(Collectors.toList());

        return ResumenCajaDTO.builder()
                .sesionId(sesion.getId())
                .estado(sesion.getEstado())
                .usuarioApertura(sesion.getUsuarioApertura() != null ? sesion.getUsuarioApertura().getUsername() : null)
                .fechaApertura(sesion.getFechaApertura())
                .montoInicial(sesion.getMontoInicial())
                .totalEfectivo(totalEfectivo)
                .totalTarjetaDebito(totalTarjetaDebito)
                .totalTarjetaCredito(totalTarjetaCredito)
                .totalTransferenciaQr(totalTransferenciaQr)
                .totalVentas(totalVentas)
                .totalIngresosCaja(totalIngresos)
                .totalEgresosCaja(totalEgresos)
                .movimientos(movimientosDTO)
                .totalEsperadoEfectivo(totalEsperadoEfectivo)
                .totalEsperadoTarjeta(totalEsperadoTarjeta)
                .totalPedidos(pedidos.size())
                .cantidadVouchers(cantidadVouchers)
                .cantidadTransferencias(cantidadTransferencias)
                .puntoExpedicion(sesion.getPuntoExpedicion() != null ? PuntoExpedicionDTO.fromEntity(sesion.getPuntoExpedicion()) : null)
                .build();
    }

    @Transactional
    public SesionCajaDTO abrirCaja(AbrirCajaRequestDTO request, String username) {
        if (sesionCajaRepository.existsByUsuarioApertura_UsernameAndEstado(username, EstadoSesionCaja.ABIERTA)) {
            throw new ReglaDeNegocioException("El usuario '" + username + "' ya tiene una sesión de caja abierta.");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        PuntoExpedicion punto = null;
        if (request != null && request.getPuntoExpedicionId() != null) {
            punto = puntoExpedicionRepository.findById(request.getPuntoExpedicionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Punto de expedición no encontrado con id " + request.getPuntoExpedicionId()));
            if (!Boolean.TRUE.equals(punto.getActivo())) {
                throw new ReglaDeNegocioException("El punto de expedición '" + punto.getNombre() + "' (" + punto.getPrefijoLegal() + ") se encuentra inactivo.");
            }
            if (sesionCajaRepository.existsByPuntoExpedicion_IdAndEstado(punto.getId(), EstadoSesionCaja.ABIERTA)) {
                throw new ReglaDeNegocioException("El punto de expedición '" + punto.getNombre() + "' (" + punto.getPrefijoLegal() + ") ya está siendo utilizado por otra caja abierta.");
            }
        } else {
            // Asignar primer punto activo disponible si existen puntos configurados
            List<PuntoExpedicion> puntosActivos = puntoExpedicionRepository.findByActivoTrueOrderByEstablecimientoAscPuntoEmisionAsc();
            for (PuntoExpedicion p : puntosActivos) {
                if (!sesionCajaRepository.existsByPuntoExpedicion_IdAndEstado(p.getId(), EstadoSesionCaja.ABIERTA)) {
                    punto = p;
                    break;
                }
            }
            if (punto == null && !puntosActivos.isEmpty()) {
                throw new ReglaDeNegocioException("Todos los puntos de expedición configurados están actualmente en uso por otros cajeros.");
            }
        }

        SesionCaja sesion = SesionCaja.builder()
                .usuarioApertura(usuario)
                .puntoExpedicion(punto)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(MonedaPyUtils.redondearGs(request.getMontoInicial()))
                .totalVentasEfectivo(BigDecimal.ZERO)
                .totalVentasTarjetaDebito(BigDecimal.ZERO)
                .totalVentasTarjetaCredito(BigDecimal.ZERO)
                .totalVentasTransferenciaQr(BigDecimal.ZERO)
                .totalVentasGeneral(BigDecimal.ZERO)
                .cantidadPedidos(0)
                .estado(EstadoSesionCaja.ABIERTA)
                .observacionesApertura(request.getObservaciones())
                .build();

        SesionCaja guardada = sesionCajaRepository.save(sesion);

        auditoriaService.registrarLog(
                username,
                "CAJA",
                "APERTURA_CAJA",
                guardada.getId(),
                "Sesión de Caja #" + guardada.getId(),
                "Apertura de turno de caja" + (guardada.getPuntoExpedicion() != null ? " en [" + guardada.getPuntoExpedicion().getPrefijoLegal() + " - " + guardada.getPuntoExpedicion().getNombre() + "]" : "") +
                ". Fondo inicial: Gs. " + guardada.getMontoInicial() +
                (request.getObservaciones() != null && !request.getObservaciones().isBlank() ? " (" + request.getObservaciones().trim() + ")" : ""),
                null
        );

        return SesionCajaDTO.fromEntity(guardada);
    }

    @Transactional
    public SesionCajaDTO cerrarCaja(CerrarCajaRequestDTO request, String username) {
        SesionCaja sesion;
        if (request != null && request.getSesionId() != null) {
            sesion = sesionCajaRepository.findById(request.getSesionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + request.getSesionId() + " no encontrada."));
            if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
                throw new ReglaDeNegocioException("La sesión de caja con id " + request.getSesionId() + " ya se encuentra cerrada.");
            }
        } else {
            sesion = sesionCajaRepository.findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(username, EstadoSesionCaja.ABIERTA)
                    .orElseThrow(() -> new ReglaDeNegocioException("No existe una sesión de caja abierta para cerrar."));
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        boolean esTitular = sesion.getUsuarioApertura() != null
                && sesion.getUsuarioApertura().getUsername() != null
                && sesion.getUsuarioApertura().getUsername().equalsIgnoreCase(username);

        boolean esAdminOSupervisor = tieneRolPrivilegiado(username);

        if (!esTitular && !esAdminOSupervisor) {
            String titular = sesion.getUsuarioApertura() != null ? sesion.getUsuarioApertura().getUsername() : "el cajero titular";
            throw new ReglaDeNegocioException("Solo el usuario que abrió la caja ('" + titular + "') o un Administrador/Supervisor pueden realizar el arqueo y cierre.");
        }

        List<Pedido> pedidos = pedidoRepository.findBySesionCaja_IdAndEstadoNot(sesion.getId(), EstadoPedido.CANCELADO);

        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjetaDebito = BigDecimal.ZERO;
        BigDecimal totalTarjetaCredito = BigDecimal.ZERO;
        BigDecimal totalTransferenciaQr = BigDecimal.ZERO;
        BigDecimal totalVentasGeneral = BigDecimal.ZERO;

        for (Pedido p : pedidos) {
            BigDecimal totalPedido = MonedaPyUtils.redondearGs(p.getTotal());
            totalVentasGeneral = totalVentasGeneral.add(totalPedido);

            if (p.getMetodoPago() == MetodoPago.EFECTIVO) {
                totalEfectivo = totalEfectivo.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_DEBITO) {
                totalTarjetaDebito = totalTarjetaDebito.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TARJETA_CREDITO) {
                totalTarjetaCredito = totalTarjetaCredito.add(totalPedido);
            } else if (p.getMetodoPago() == MetodoPago.TRANSFERENCIA_QR) {
                totalTransferenciaQr = totalTransferenciaQr.add(totalPedido);
            }
        }

        BigDecimal totalEsperadoTarjeta = MonedaPyUtils.redondearGs(totalTarjetaDebito.add(totalTarjetaCredito));

        BigDecimal sumIngresos = movimientoCajaRepository.sumMontoBySesionCajaIdAndTipo(sesion.getId(), TipoMovimientoCaja.INGRESO);
        BigDecimal sumEgresos = movimientoCajaRepository.sumMontoBySesionCajaIdAndTipo(sesion.getId(), TipoMovimientoCaja.EGRESO);
        BigDecimal totalIngresos = sumIngresos != null ? MonedaPyUtils.redondearGs(sumIngresos) : BigDecimal.ZERO;
        BigDecimal totalEgresos = sumEgresos != null ? MonedaPyUtils.redondearGs(sumEgresos) : BigDecimal.ZERO;

        BigDecimal montoEsperadoEfectivo = MonedaPyUtils.redondearGs(
                sesion.getMontoInicial().add(totalEfectivo).add(totalIngresos).subtract(totalEgresos)
        );
        BigDecimal montoRealEfectivo = MonedaPyUtils.redondearGs(request.getMontoRealEfectivo());
        BigDecimal diferencia = MonedaPyUtils.redondearGs(montoRealEfectivo.subtract(montoEsperadoEfectivo));

        BigDecimal montoRealTarjeta = request.getMontoRealTarjeta() != null
                ? MonedaPyUtils.redondearGs(request.getMontoRealTarjeta())
                : null;
        BigDecimal diferenciaTarjeta = montoRealTarjeta != null
                ? MonedaPyUtils.redondearGs(montoRealTarjeta.subtract(totalEsperadoTarjeta))
                : null;

        BigDecimal montoRealTransferenciaQr = request.getMontoRealTransferenciaQr() != null
                ? MonedaPyUtils.redondearGs(request.getMontoRealTransferenciaQr())
                : null;
        BigDecimal diferenciaTransferenciaQr = montoRealTransferenciaQr != null
                ? MonedaPyUtils.redondearGs(montoRealTransferenciaQr.subtract(totalTransferenciaQr))
                : null;

        sesion.setTotalIngresosCaja(totalIngresos);
        sesion.setTotalEgresosCaja(totalEgresos);

        sesion.setUsuarioCierre(usuario);
        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setMontoEsperadoEfectivo(montoEsperadoEfectivo);
        sesion.setMontoRealEfectivo(montoRealEfectivo);
        sesion.setDiferencia(diferencia);
        sesion.setMontoRealTarjeta(montoRealTarjeta);
        sesion.setDiferenciaTarjeta(diferenciaTarjeta);
        sesion.setMontoRealTransferenciaQr(montoRealTransferenciaQr);
        sesion.setDiferenciaTransferenciaQr(diferenciaTransferenciaQr);
        sesion.setTotalVentasEfectivo(totalEfectivo);
        sesion.setTotalVentasTarjetaDebito(totalTarjetaDebito);
        sesion.setTotalVentasTarjetaCredito(totalTarjetaCredito);
        sesion.setTotalVentasTransferenciaQr(totalTransferenciaQr);
        sesion.setTotalVentasGeneral(totalVentasGeneral);
        sesion.setCantidadPedidos(pedidos.size());
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        sesion.setObservacionesCierre(request.getObservaciones());

        SesionCaja guardada = sesionCajaRepository.save(sesion);

        String detalleCierre = String.format(
                "Cierre de caja. Efectivo declarado: Gs. %s (Esperado: Gs. %s, Diferencia: Gs. %s). Ventas totales: Gs. %s (%d pedidos)",
                guardada.getMontoRealEfectivo(),
                guardada.getMontoEsperadoEfectivo(),
                guardada.getDiferencia(),
                guardada.getTotalVentasGeneral(),
                guardada.getCantidadPedidos()
        );

        auditoriaService.registrarLog(
                username,
                "CAJA",
                "CIERRE_CAJA",
                guardada.getId(),
                "Sesión de Caja #" + guardada.getId(),
                detalleCierre,
                null
        );

        return SesionCajaDTO.fromEntity(guardada);
    }

    @Transactional(readOnly = true)
    public List<SesionCajaDTO> obtenerSesionesAbiertas() {
        return sesionCajaRepository.findByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .stream()
                .map(SesionCajaDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SesionCajaDTO> obtenerHistorial() {
        return sesionCajaRepository.findAllByOrderByFechaAperturaDesc()
                .stream()
                .map(SesionCajaDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public SesionCajaDTO obtenerPorId(Long id) {
        return sesionCajaRepository.findById(id)
                .map(SesionCajaDTO::fromEntity)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + id + " no encontrada."));
    }

    @Transactional
    public MovimientoCajaDTO registrarMovimiento(RegistrarMovimientoCajaRequestDTO request, String username) {
        if (request == null) {
            throw new ReglaDeNegocioException("La solicitud de movimiento no puede ser nula.");
        }
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaDeNegocioException("El monto del movimiento debe ser mayor a cero.");
        }
        if (request.getConcepto() == null || request.getConcepto().trim().isBlank()) {
            throw new ReglaDeNegocioException("Debe especificar el concepto o motivo del movimiento.");
        }

        SesionCaja sesion;
        if (request.getSesionId() != null) {
            sesion = sesionCajaRepository.findById(request.getSesionId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + request.getSesionId() + " no encontrada."));
            if (username != null) {
                verificarAccesoSesion(sesion, username);
            }
        } else {
            sesion = sesionCajaRepository.findFirstByUsuarioApertura_UsernameAndEstadoOrderByFechaAperturaDesc(username, EstadoSesionCaja.ABIERTA)
                    .orElseGet(() -> sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                            .orElseThrow(() -> new ReglaDeNegocioException("No existe ninguna sesión de caja abierta para registrar movimientos.")));
        }

        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new ReglaDeNegocioException("No se pueden registrar movimientos en una sesión de caja cerrada (Sesión #" + sesion.getId() + ").");
        }

        BigDecimal montoRedondeado = MonedaPyUtils.redondearGs(request.getMonto());

        MovimientoCaja mov = MovimientoCaja.builder()
                .sesionCaja(sesion)
                .tipo(request.getTipo())
                .categoria(request.getCategoria())
                .concepto(request.getConcepto().trim())
                .comprobante(request.getComprobante() != null && !request.getComprobante().isBlank() ? request.getComprobante().trim() : null)
                .monto(montoRedondeado)
                .usuario(username != null ? username : "sistema")
                .fecha(LocalDateTime.now())
                .build();

        MovimientoCaja guardado = movimientoCajaRepository.save(mov);

        if (request.getTipo() == TipoMovimientoCaja.INGRESO) {
            sesion.setTotalIngresosCaja(sesion.getTotalIngresosCaja().add(montoRedondeado));
        } else {
            sesion.setTotalEgresosCaja(sesion.getTotalEgresosCaja().add(montoRedondeado));
        }
        sesionCajaRepository.save(sesion);

        auditoriaService.registrarLog(
                username != null ? username : "sistema",
                "CAJA",
                request.getTipo() == TipoMovimientoCaja.EGRESO ? "EGRESO_CAJA" : "INGRESO_CAJA",
                guardado.getId(),
                "Movimiento de Caja #" + guardado.getId(),
                String.format("%s: Gs. %s - %s [%s] (Sesión #%d)",
                        request.getTipo(),
                        guardado.getMonto(),
                        guardado.getConcepto(),
                        guardado.getCategoria(),
                        sesion.getId()),
                null
        );

        return MovimientoCajaDTO.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaDTO> listarMovimientos(Long sesionId) {
        if (sesionId == null) {
            return List.of();
        }
        return movimientoCajaRepository.findBySesionCaja_IdOrderByFechaDesc(sesionId)
                .stream()
                .map(MovimientoCajaDTO::fromEntity)
                .toList();
    }

    // ---------------------------------------------------------------------------------------------
    // Aislamiento entre cajas: solo el titular de la sesión o un rol privilegiado puede verla u operarla.
    // ---------------------------------------------------------------------------------------------

    private static final java.util.Set<String> ROLES_PRIVILEGIADOS = java.util.Set.of("ADMIN", "OWNER", "SUPERVISOR");

    private static String normalizarRol(String rol) {
        String r = rol == null ? "" : rol.trim().toUpperCase();
        return r.startsWith("ROLE_") ? r.substring(5) : r;
    }

    boolean tieneRolPrivilegiado(String username) {
        boolean porRol = username != null && usuarioRepository.findByUsername(username)
                .map(u -> u.getRol() != null && ROLES_PRIVILEGIADOS.contains(normalizarRol(u.getRol().getNombreRol())))
                .orElse(false);
        if (porRol) {
            return true;
        }
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities() != null && auth.getAuthorities().stream()
                .anyMatch(a -> ROLES_PRIVILEGIADOS.contains(normalizarRol(a.getAuthority())));
    }

    private void verificarAccesoSesion(SesionCaja sesion, String username) {
        boolean esTitular = sesion.getUsuarioApertura() != null
                && sesion.getUsuarioApertura().getUsername() != null
                && sesion.getUsuarioApertura().getUsername().equalsIgnoreCase(username);
        if (!esTitular && !tieneRolPrivilegiado(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "El usuario '" + username + "' no puede acceder a la sesión de caja #" + sesion.getId() + ".");
        }
    }

    @Transactional(readOnly = true)
    public SesionCajaDTO obtenerPorId(Long id, String username) {
        SesionCaja sesion = sesionCajaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + id + " no encontrada."));
        verificarAccesoSesion(sesion, username);
        return SesionCajaDTO.fromEntity(sesion);
    }

    @Transactional(readOnly = true)
    public List<MovimientoCajaDTO> listarMovimientos(Long sesionId, String username) {
        if (sesionId == null) {
            return List.of();
        }
        SesionCaja sesion = sesionCajaRepository.findById(sesionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sesión de caja con id " + sesionId + " no encontrada."));
        verificarAccesoSesion(sesion, username);
        return listarMovimientos(sesionId);
    }

    /** Un cajero ve solo su historial; ADMIN/OWNER/SUPERVISOR ven todo. */
    @Transactional(readOnly = true)
    public List<SesionCajaDTO> obtenerHistorial(String username) {
        if (tieneRolPrivilegiado(username)) {
            return obtenerHistorial();
        }
        return sesionCajaRepository.findAllByOrderByFechaAperturaDesc().stream()
                .filter(s -> s.getUsuarioApertura() != null && username.equalsIgnoreCase(s.getUsuarioApertura().getUsername()))
                .map(SesionCajaDTO::fromEntity)
                .toList();
    }
}
