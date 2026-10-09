package com.heladeria.api.services;

import com.heladeria.api.dto.*;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.util.MonedaPyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CajaService {

    private final SesionCajaRepository sesionCajaRepository;
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public CajaService(SesionCajaRepository sesionCajaRepository,
                       PedidoRepository pedidoRepository,
                       UsuarioRepository usuarioRepository) {
        this.sesionCajaRepository = sesionCajaRepository;
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public EstadoCajaDTO obtenerEstado() {
        return sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .map(s -> new EstadoCajaDTO(
                        true,
                        s.getId(),
                        s.getUsuarioApertura() != null ? s.getUsuarioApertura().getUsername() : null,
                        s.getFechaApertura(),
                        s.getMontoInicial()
                ))
                .orElseGet(() -> new EstadoCajaDTO(false, null, null, null, null));
    }

    @Transactional(readOnly = true)
    public ResumenCajaDTO obtenerResumenActual() {
        SesionCaja sesion = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe una sesión de caja abierta actualmente."));

        List<Pedido> pedidos = pedidoRepository.findBySesionCajaIdAndEstadoNot(sesion.getId(), EstadoPedido.CANCELADO);

        BigDecimal totalEfectivo = BigDecimal.ZERO;
        BigDecimal totalTarjetaDebito = BigDecimal.ZERO;
        BigDecimal totalTarjetaCredito = BigDecimal.ZERO;
        BigDecimal totalTransferenciaQr = BigDecimal.ZERO;
        BigDecimal totalVentas = BigDecimal.ZERO;

        for (Pedido p : pedidos) {
            BigDecimal totalPedido = MonedaPyUtils.redondearGs(p.getTotal());
            totalVentas = totalVentas.add(totalPedido);

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

        BigDecimal totalEsperadoEfectivo = MonedaPyUtils.redondearGs(sesion.getMontoInicial().add(totalEfectivo));

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
                .totalEsperadoEfectivo(totalEsperadoEfectivo)
                .totalPedidos(pedidos.size())
                .build();
    }

    @Transactional
    public SesionCajaDTO abrirCaja(AbrirCajaRequestDTO request, String username) {
        if (sesionCajaRepository.existsByEstado(EstadoSesionCaja.ABIERTA)) {
            throw new ReglaDeNegocioException("Ya existe una caja abierta en el sistema.");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        SesionCaja sesion = SesionCaja.builder()
                .usuarioApertura(usuario)
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
        return SesionCajaDTO.fromEntity(guardada);
    }

    @Transactional
    public SesionCajaDTO cerrarCaja(CerrarCajaRequestDTO request, String username) {
        SesionCaja sesion = sesionCajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe una sesión de caja abierta para cerrar."));

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));

        List<Pedido> pedidos = pedidoRepository.findBySesionCajaIdAndEstadoNot(sesion.getId(), EstadoPedido.CANCELADO);

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

        BigDecimal montoEsperadoEfectivo = MonedaPyUtils.redondearGs(sesion.getMontoInicial().add(totalEfectivo));
        BigDecimal montoRealEfectivo = MonedaPyUtils.redondearGs(request.getMontoRealEfectivo());
        BigDecimal diferencia = MonedaPyUtils.redondearGs(montoRealEfectivo.subtract(montoEsperadoEfectivo));

        sesion.setUsuarioCierre(usuario);
        sesion.setFechaCierre(LocalDateTime.now());
        sesion.setMontoEsperadoEfectivo(montoEsperadoEfectivo);
        sesion.setMontoRealEfectivo(montoRealEfectivo);
        sesion.setDiferencia(diferencia);
        sesion.setTotalVentasEfectivo(totalEfectivo);
        sesion.setTotalVentasTarjetaDebito(totalTarjetaDebito);
        sesion.setTotalVentasTarjetaCredito(totalTarjetaCredito);
        sesion.setTotalVentasTransferenciaQr(totalTransferenciaQr);
        sesion.setTotalVentasGeneral(totalVentasGeneral);
        sesion.setCantidadPedidos(pedidos.size());
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        sesion.setObservacionesCierre(request.getObservaciones());

        SesionCaja guardada = sesionCajaRepository.save(sesion);
        return SesionCajaDTO.fromEntity(guardada);
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
}
