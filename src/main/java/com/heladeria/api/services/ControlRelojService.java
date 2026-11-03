package com.heladeria.api.services;

import com.heladeria.api.entities.ControlReloj;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.ControlRelojRepository;
import com.heladeria.api.repositories.PedidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Detecta si el reloj del servidor retrocedió (alguien lo atrasó para extender la licencia).
 * Guarda la fecha más alta vista y la compara también con el pedido más reciente, así que borrar el control
 * no alcanza para ocultarlo. Es solo un aviso: no bloquea la operación.
 */
@Service
public class ControlRelojService {

    private static final Logger log = LoggerFactory.getLogger(ControlRelojService.class);
    private static final Long ID_UNICO = 1L;

    private final ControlRelojRepository repository;
    private final PedidoRepository pedidoRepository;
    private final AuditoriaService auditoriaService;

    /** Margen para ajustes normales de NTP u horario de verano. */
    @Value("${app.licencia.tolerancia-reloj-minutos:10}")
    private long toleranciaMinutos;

    public ControlRelojService(ControlRelojRepository repository, PedidoRepository pedidoRepository, AuditoriaService auditoriaService) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.auditoriaService = auditoriaService;
    }

    public ControlReloj registrar() {
        return registrar(LocalDateTime.now());
    }

    @Transactional
    public ControlReloj registrar(LocalDateTime ahora) {
        ControlReloj c = repository.findById(ID_UNICO).orElseGet(ControlReloj::new);
        LocalDateTime piso = pisoConocido(c);

        if (piso != null && ahora.isBefore(piso.minusMinutes(toleranciaMinutos))) {
            // Reloj atrasado: se conserva la evidencia y NO se avanza la fecha vista
            if (!c.isRetrocesoDetectado()) {
                c.setRetrocesoDetectado(true);
                c.setFechaRetroceso(ahora);
                c.setReferenciaRetroceso(piso);
                c.setDesfaseMinutos(Duration.between(ahora, piso).toMinutes());
                log.error("RELOJ RETROCEDIDO: el reloj marca {} pero el sistema ya vio {} ({} min de diferencia).",
                        ahora, piso, c.getDesfaseMinutos());
                auditoriaService.registrarLog("sistema", "LICENCIA", "ALERTA", null, "Reloj del servidor",
                        "Retroceso de reloj detectado: marca " + ahora + " y ya se vio " + piso + " (" + c.getDesfaseMinutos() + " min).",
                        "localhost");
            }
        } else if (c.getUltimaFechaVista() == null || ahora.isAfter(c.getUltimaFechaVista())) {
            c.setUltimaFechaVista(ahora);
        }
        return repository.save(c);
    }

    /** El OWNER confirma que ya corrigió el reloj; solo se acepta si el reloj efectivamente alcanzó la referencia. */
    @Transactional
    public void reconocerRetroceso(String usuario) {
        reconocerRetroceso(usuario, LocalDateTime.now());
    }

    @Transactional
    public void reconocerRetroceso(String usuario, LocalDateTime ahora) {
        ControlReloj c = repository.findById(ID_UNICO).orElseGet(ControlReloj::new);
        if (!c.isRetrocesoDetectado()) {
            return;
        }
        LocalDateTime piso = pisoConocido(c);
        if (piso != null && ahora.isBefore(piso.minusMinutes(toleranciaMinutos))) {
            throw new ReglaDeNegocioException("El reloj del servidor sigue atrasado (marca " + ahora + " y ya se vio " + piso
                    + "). Corrija la hora del servidor antes de reconocer la alerta.");
        }
        c.setRetrocesoDetectado(false);
        c.setFechaRetroceso(null);
        c.setReferenciaRetroceso(null);
        c.setDesfaseMinutos(null);
        c.setUltimaFechaVista(ahora);
        repository.save(c);
        auditoriaService.registrarLog(usuario, "LICENCIA", "MODIFICAR", null, "Reloj del servidor",
                "Se reconoció la alerta de retroceso de reloj tras corregir la hora.", "localhost");
    }

    @Transactional(readOnly = true)
    public ControlReloj obtener() {
        return repository.findById(ID_UNICO).orElseGet(ControlReloj::new);
    }

    private LocalDateTime pisoConocido(ControlReloj c) {
        LocalDateTime piso = c.getUltimaFechaVista();
        LocalDateTime ultimoPedido = pedidoRepository.maxFechaCreacion();
        if (ultimoPedido != null && (piso == null || ultimoPedido.isAfter(piso))) {
            piso = ultimoPedido;
        }
        return piso;
    }
}
