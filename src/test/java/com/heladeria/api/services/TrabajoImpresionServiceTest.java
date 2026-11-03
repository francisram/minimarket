package com.heladeria.api.services;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.TrabajoImpresion;
import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import com.heladeria.api.exceptions.ImpresionException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.TrabajoImpresionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Spooler de impresión: reintentos con backoff, dead letter, recuperación y atomicidad con la venta. */
@SpringBootTest
@DisplayName("Spooler de impresión")
class TrabajoImpresionServiceTest {

    @Autowired private TrabajoImpresionService service;
    @Autowired private TrabajoImpresionRepository repository;
    @Autowired private PlatformTransactionManager transactionManager;

    @MockBean private ImpresionService impresionService;
    @MockBean private TicketPrinterService ticketPrinterService;
    @MockBean private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(new Pedido()));
        when(ticketPrinterService.resolverImpresora(any())).thenReturn(new Impresora());
        when(ticketPrinterService.construirTicketPedido(any())).thenReturn(new byte[]{1, 2, 3});
    }

    @AfterEach
    void limpiar() {
        repository.deleteAll();
    }

    private TrabajoImpresion encolar() {
        return service.encolarTicketPedido(1L, null, "cajero");
    }

    @Test
    @DisplayName("Un trabajo pendiente se imprime y queda IMPRESO")
    void imprimeConExito() {
        TrabajoImpresion t = encolar();

        assertEquals(1, service.procesarPendientes(10));

        TrabajoImpresion r = repository.findById(t.getId()).orElseThrow();
        assertEquals(EstadoTrabajoImpresion.IMPRESO, r.getEstado());
        assertEquals(1, r.getIntentos());
        verify(impresionService, times(1)).imprimir(any(Impresora.class), any(byte[].class));
    }

    @Test
    @DisplayName("Si la impresora falla, el trabajo vuelve a PENDIENTE con espera (backoff) y no se reintenta de inmediato")
    void falloTransitorioReintentaConBackoff() {
        doThrow(new ImpresionException("sin papel")).when(impresionService).imprimir(any(Impresora.class), any(byte[].class));
        TrabajoImpresion t = encolar();

        service.procesarPendientes(10);

        TrabajoImpresion r = repository.findById(t.getId()).orElseThrow();
        assertEquals(EstadoTrabajoImpresion.PENDIENTE, r.getEstado());
        assertEquals(1, r.getIntentos());
        assertEquals("sin papel", r.getUltimoError());
        assertTrue(r.getProximoIntento().isAfter(LocalDateTime.now().plusSeconds(2)));

        assertEquals(0, service.procesarPendientes(10), "Aún no toca el siguiente intento");
        verify(impresionService, times(1)).imprimir(any(Impresora.class), any(byte[].class));
    }

    @Test
    @DisplayName("Al agotar los intentos pasa a MUERTO (dead letter) y se puede reencolar manualmente")
    void agotaIntentosYSeReencola() {
        doThrow(new ImpresionException("IP inalcanzable")).when(impresionService).imprimir(any(Impresora.class), any(byte[].class));
        TrabajoImpresion t = encolar();
        t.setMaxIntentos(1);
        repository.save(t);

        service.procesarPendientes(10);

        TrabajoImpresion muerto = repository.findById(t.getId()).orElseThrow();
        assertEquals(EstadoTrabajoImpresion.MUERTO, muerto.getEstado());

        // Con la impresora recuperada, el reintento manual lo imprime
        doNothing().when(impresionService).imprimir(any(Impresora.class), any(byte[].class));
        service.reintentar(t.getId());
        service.procesarPendientes(10);
        assertEquals(EstadoTrabajoImpresion.IMPRESO, repository.findById(t.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Si el pedido ya no existe el trabajo va directo a MUERTO sin reintentos inútiles")
    void pedidoInexistenteEsIrrecuperable() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());
        TrabajoImpresion t = encolar();

        service.procesarPendientes(10);

        TrabajoImpresion r = repository.findById(t.getId()).orElseThrow();
        assertEquals(EstadoTrabajoImpresion.MUERTO, r.getEstado());
        assertEquals(1, r.getIntentos());
        verifyNoInteractions(impresionService);
    }

    @Test
    @DisplayName("Un trabajo atascado en IMPRIMIENDO (caída de la API) se recupera como PENDIENTE")
    void recuperaAtascados() {
        TrabajoImpresion t = encolar();
        t.setEstado(EstadoTrabajoImpresion.IMPRIMIENDO);
        t.setFechaActualizacion(LocalDateTime.now().minusMinutes(10));
        repository.save(t);

        service.recuperarAtascados();

        assertEquals(EstadoTrabajoImpresion.PENDIENTE, repository.findById(t.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Outbox: si la transacción de la venta hace rollback, no queda ningún trabajo de impresión")
    void rollbackDeLaVentaDescartaElTrabajo() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            encolar();
            status.setRollbackOnly();
        });
        assertEquals(0, repository.count());
    }

    @Test
    @DisplayName("Solo se pueden reintentar trabajos MUERTOS")
    void reintentarSoloMuertos() {
        TrabajoImpresion t = encolar();
        assertThrows(ReglaDeNegocioException.class, () -> service.reintentar(t.getId()));
    }

    @Test
    @DisplayName("El backoff crece y se estabiliza en 30 minutos")
    void backoffExponencial() {
        assertEquals(5, TrabajoImpresionService.esperaSegundos(1));
        assertEquals(60, TrabajoImpresionService.esperaSegundos(3));
        assertEquals(1800, TrabajoImpresionService.esperaSegundos(6));
        assertEquals(1800, TrabajoImpresionService.esperaSegundos(20));
    }

    private EstadoTrabajoImpresion estadoDe(Long id) {
        return repository.findById(id).orElseThrow().getEstado();
    }

    @Test
    @DisplayName("Una impresora caída no retrasa a las demás: cada impresora tiene su propio carril")
    void impresoraCaidaNoFrenaALasDemas() throws Exception {
        Impresora caida = new Impresora();
        caida.setNombre("CAIDA");
        Impresora sana = new Impresora();
        sana.setNombre("SANA");
        when(ticketPrinterService.resolverImpresora(1L)).thenReturn(caida);
        when(ticketPrinterService.resolverImpresora(2L)).thenReturn(sana);
        doAnswer(inv -> {
            Impresora i = inv.getArgument(0);
            if ("CAIDA".equals(i.getNombre())) {
                Thread.sleep(1500);
                throw new ImpresionException("conexión rechazada");
            }
            return null;
        }).when(impresionService).imprimir(any(Impresora.class), any(byte[].class));

        TrabajoImpresion a1 = service.encolarTicketPedido(1L, 1L, "c");
        TrabajoImpresion a2 = service.encolarTicketPedido(2L, 1L, "c");
        TrabajoImpresion b1 = service.encolarTicketPedido(3L, 2L, "c");

        assertEquals(3, service.procesarPendientesAsync(10));

        // La impresora sana termina enseguida, mientras la caída sigue ocupada con su primer trabajo
        long limite = System.currentTimeMillis() + 1000;
        while (estadoDe(b1.getId()) != EstadoTrabajoImpresion.IMPRESO && System.currentTimeMillis() < limite) {
            Thread.sleep(20);
        }
        assertEquals(EstadoTrabajoImpresion.IMPRESO, estadoDe(b1.getId()), "El ticket de la impresora sana no esperó a la caída");
        assertEquals(EstadoTrabajoImpresion.IMPRIMIENDO, estadoDe(a1.getId()));

        // Mientras el carril de la impresora caída está ocupado no se le asignan más trabajos, pero sí a las demás
        TrabajoImpresion a3 = service.encolarTicketPedido(4L, 1L, "c");
        TrabajoImpresion b2 = service.encolarTicketPedido(5L, 2L, "c");
        assertEquals(1, service.procesarPendientesAsync(10));
        long limite2 = System.currentTimeMillis() + 1000;
        while (estadoDe(b2.getId()) != EstadoTrabajoImpresion.IMPRESO && System.currentTimeMillis() < limite2) {
            Thread.sleep(20);
        }
        assertEquals(EstadoTrabajoImpresion.IMPRESO, estadoDe(b2.getId()));
        assertEquals(EstadoTrabajoImpresion.PENDIENTE, estadoDe(a3.getId()), "El carril ocupado no recibió otro trabajo");

        // Al terminar, los trabajos de la impresora caída quedan reprogramados con su espera
        long limite3 = System.currentTimeMillis() + 6000;
        while (estadoDe(a2.getId()) == EstadoTrabajoImpresion.IMPRIMIENDO && System.currentTimeMillis() < limite3) {
            Thread.sleep(50);
        }
        assertEquals(EstadoTrabajoImpresion.PENDIENTE, estadoDe(a1.getId()));
        assertEquals(1, repository.findById(a1.getId()).orElseThrow().getIntentos());
        assertEquals(EstadoTrabajoImpresion.PENDIENTE, estadoDe(a2.getId()));
    }
}
