package com.heladeria.api.services;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.TrabajoImpresion;
import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.TrabajoImpresionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Spooler de impresión: cola persistente con reintentos exponenciales y dead letter.
 * <p>
 * Entrega "al menos una vez": si la API se cae justo mientras imprime, el trabajo se recupera
 * como pendiente y el ticket podría salir duplicado, pero nunca se pierde.
 * La E/S de red nunca ocurre dentro de una transacción de base de datos.
 */
@Service
public class TrabajoImpresionService {

    private static final Logger log = LoggerFactory.getLogger(TrabajoImpresionService.class);
    public static final String TIPO_TICKET_PEDIDO = "TICKET_PEDIDO";
    private static final int[] ESPERA_SEGUNDOS = {5, 15, 60, 300, 900, 1800};
    private static final Duration TIEMPO_MAXIMO_IMPRIMIENDO = Duration.ofMinutes(2);

    private final TrabajoImpresionRepository repository;
    private final PedidoRepository pedidoRepository;
    private final TicketPrinterService ticketPrinterService;
    private final ImpresionService impresionService;
    private final TransactionTemplate tx;

    public TrabajoImpresionService(TrabajoImpresionRepository repository,
                                   PedidoRepository pedidoRepository,
                                   TicketPrinterService ticketPrinterService,
                                   ImpresionService impresionService,
                                   PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.pedidoRepository = pedidoRepository;
        this.ticketPrinterService = ticketPrinterService;
        this.impresionService = impresionService;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /** Se une a la transacción de la venta: si la venta hace rollback, el trabajo también. */
    @Transactional
    public TrabajoImpresion encolarTicketPedido(Long pedidoId, Long impresoraId, String usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        TrabajoImpresion t = new TrabajoImpresion();
        t.setTipo(TIPO_TICKET_PEDIDO);
        t.setReferenciaId(pedidoId);
        t.setImpresoraId(impresoraId);
        t.setEstado(EstadoTrabajoImpresion.PENDIENTE);
        t.setIntentos(0);
        t.setProximoIntento(ahora);
        t.setUsuario(usuario);
        t.setFechaCreacion(ahora);
        t.setFechaActualizacion(ahora);
        return repository.save(t);
    }

    /** Procesa hasta {@code lote} trabajos listos. Devuelve cuántos intentó. */
    public int procesarPendientes(int lote) {
        recuperarAtascados();
        List<Long> ids = tx.execute(status -> {
            LocalDateTime ahora = LocalDateTime.now();
            List<TrabajoImpresion> reclamados = repository.reclamar(EstadoTrabajoImpresion.PENDIENTE, ahora, PageRequest.of(0, lote));
            for (TrabajoImpresion t : reclamados) {
                t.setEstado(EstadoTrabajoImpresion.IMPRIMIENDO);
                t.setIntentos(t.getIntentos() + 1);
                t.setFechaActualizacion(ahora);
            }
            return reclamados.stream().map(TrabajoImpresion::getId).toList();
        });
        if (ids == null) {
            return 0;
        }
        ids.forEach(this::imprimir);
        return ids.size();
    }

    private record Preparado(Impresora impresora, byte[] datos) { }

    private void imprimir(Long trabajoId) {
        Preparado preparado;
        try {
            preparado = tx.execute(status -> {
                TrabajoImpresion t = repository.findById(trabajoId).orElseThrow();
                Pedido pedido = pedidoRepository.findById(t.getReferenciaId())
                        .orElseThrow(() -> new IllegalStateException("El pedido #" + t.getReferenciaId() + " ya no existe"));
                return new Preparado(ticketPrinterService.resolverImpresora(t.getImpresoraId()),
                        ticketPrinterService.construirTicketPedido(pedido));
            });
        } catch (IllegalStateException e) {
            registrarResultado(trabajoId, e.getMessage(), true);
            return;
        } catch (Exception e) {
            registrarResultado(trabajoId, mensaje(e), false);
            return;
        }

        try {
            impresionService.imprimir(preparado.impresora(), preparado.datos());
            registrarResultado(trabajoId, null, false);
        } catch (Exception e) {
            registrarResultado(trabajoId, mensaje(e), false);
        }
    }

    private void registrarResultado(Long trabajoId, String error, boolean irrecuperable) {
        tx.executeWithoutResult(status -> {
            TrabajoImpresion t = repository.findById(trabajoId).orElseThrow();
            LocalDateTime ahora = LocalDateTime.now();
            t.setFechaActualizacion(ahora);
            if (error == null) {
                t.setEstado(EstadoTrabajoImpresion.IMPRESO);
                t.setUltimoError(null);
                log.info("Trabajo de impresión #{} completado (intento {})", t.getId(), t.getIntentos());
                return;
            }
            t.setUltimoError(error.length() > 500 ? error.substring(0, 500) : error);
            if (irrecuperable || t.getIntentos() >= t.getMaxIntentos()) {
                t.setEstado(EstadoTrabajoImpresion.MUERTO);
                log.error("Trabajo de impresión #{} pasa a dead letter tras {} intento(s): {}", t.getId(), t.getIntentos(), error);
            } else {
                t.setEstado(EstadoTrabajoImpresion.PENDIENTE);
                t.setProximoIntento(ahora.plusSeconds(esperaSegundos(t.getIntentos())));
                log.warn("Trabajo de impresión #{} falló (intento {}/{}): {}", t.getId(), t.getIntentos(), t.getMaxIntentos(), error);
            }
        });
    }

    /** Backoff exponencial: 5 s, 15 s, 1 min, 5 min, 15 min, 30 min. */
    static long esperaSegundos(int intentos) {
        int idx = Math.max(0, Math.min(intentos - 1, ESPERA_SEGUNDOS.length - 1));
        return ESPERA_SEGUNDOS[idx];
    }

    /** Trabajos que quedaron en IMPRIMIENDO por una caída de la API vuelven a la cola. */
    public void recuperarAtascados() {
        tx.executeWithoutResult(status -> {
            LocalDateTime ahora = LocalDateTime.now();
            for (TrabajoImpresion t : repository.findByEstadoAndFechaActualizacionBefore(
                    EstadoTrabajoImpresion.IMPRIMIENDO, ahora.minus(TIEMPO_MAXIMO_IMPRIMIENDO))) {
                t.setEstado(EstadoTrabajoImpresion.PENDIENTE);
                t.setProximoIntento(ahora);
                t.setFechaActualizacion(ahora);
                log.warn("Trabajo de impresión #{} recuperado tras quedar atascado", t.getId());
            }
        });
    }

    @Transactional
    public TrabajoImpresion reintentar(Long id) {
        TrabajoImpresion t = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Trabajo de impresión " + id + " no encontrado."));
        if (t.getEstado() != EstadoTrabajoImpresion.MUERTO) {
            throw new ReglaDeNegocioException("Solo se pueden reintentar trabajos en estado MUERTO (actual: " + t.getEstado() + ").");
        }
        LocalDateTime ahora = LocalDateTime.now();
        t.setEstado(EstadoTrabajoImpresion.PENDIENTE);
        t.setIntentos(0);
        t.setProximoIntento(ahora);
        t.setFechaActualizacion(ahora);
        return t;
    }

    @Transactional(readOnly = true)
    public List<TrabajoImpresion> listar(EstadoTrabajoImpresion estado) {
        return estado != null ? repository.findByEstadoOrderByFechaCreacionDesc(estado) : repository.findTop100ByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Map<EstadoTrabajoImpresion, Long> resumen() {
        Map<EstadoTrabajoImpresion, Long> r = new EnumMap<>(EstadoTrabajoImpresion.class);
        for (EstadoTrabajoImpresion e : EstadoTrabajoImpresion.values()) {
            r.put(e, repository.countByEstado(e));
        }
        return r;
    }

    private static String mensaje(Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
