package com.heladeria.api.concurrency;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.enums.EstadoTrabajoImpresion;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.TrabajoImpresionRepository;
import com.heladeria.api.services.ImpresionService;
import com.heladeria.api.services.TicketPrinterService;
import com.heladeria.api.services.TrabajoImpresionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Varios workers sobre PostgreSQL real: SKIP LOCKED debe repartir los trabajos sin duplicar impresiones. */
@SpringBootTest
@Testcontainers
class SpoolerConcurrenciaIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired TrabajoImpresionService service;
    @Autowired TrabajoImpresionRepository repository;
    @MockBean ImpresionService impresionService;
    @MockBean TicketPrinterService ticketPrinterService;
    @MockBean PedidoRepository pedidoRepository;

    @Test
    @DisplayName("Dos workers concurrentes imprimen cada trabajo exactamente una vez")
    void workersConcurrentesNoDuplican() throws Exception {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.of(new Pedido()));
        when(ticketPrinterService.resolverImpresora(any())).thenReturn(new Impresora());
        when(ticketPrinterService.construirTicketPedido(any())).thenReturn(new byte[]{1});
        // Impresión lenta para que los workers realmente se solapen
        doAnswer(inv -> { Thread.sleep(20); return null; }).when(impresionService).imprimir(any(Impresora.class), any(byte[].class));

        int total = 30;
        for (int i = 0; i < total; i++) {
            service.encolarTicketPedido((long) i + 1, null, "cajero");
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrera = new CyclicBarrier(2);
        Callable<Integer> worker = () -> {
            barrera.await();
            int procesados = 0;
            int n;
            while ((n = service.procesarPendientes(5)) > 0) {
                procesados += n;
            }
            return procesados;
        };
        Future<Integer> a = pool.submit(worker);
        Future<Integer> b = pool.submit(worker);
        int suma = a.get(60, TimeUnit.SECONDS) + b.get(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(total, suma, "Cada trabajo se procesa una sola vez entre los dos workers");
        verify(impresionService, times(total)).imprimir(any(Impresora.class), any(byte[].class));
        assertEquals(total, repository.countByEstado(EstadoTrabajoImpresion.IMPRESO));
    }
}
