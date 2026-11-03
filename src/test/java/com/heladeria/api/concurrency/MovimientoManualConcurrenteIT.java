package com.heladeria.api.concurrency;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.services.InventarioService;
import com.heladeria.api.dto.inventario.RegistrarMovimientoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de concurrencia contra PostgreSQL real (H2 no reproduce el aislamiento READ COMMITTED).
 * Cada hilo vende con su propio usuario y su propia sesión de caja, como en producción.
 * Sin @Transactional: cada venta debe commitear en su propia transacción.
 */
@SpringBootTest
@Testcontainers
class MovimientoManualConcurrenteIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        // El esquema lo crea Flyway y Hibernate solo lo valida: prueba V1/V2 contra las entidades.
        // Con PostgreSQL la validación es estricta: secreto JWT y clave de cifrado deben estar definidos
        r.add("app.jwt.secret", () -> "q8F2xk0Lr9mZp3TvN7yB1cHs5WdE6gJaUoX4iKtRnMb2VfYe8ZlQwP0uSh3DjCaA");
        r.add("app.seguridad.clave-cifrado", () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired InventarioService inventarioService;
    @Autowired ProductoSimpleRepository productoSimpleRepository;

    @Test
    @DisplayName("8 entradas manuales simultaneas suman 8: no se pierden actualizaciones")
    void entradasSimultaneasNoPierdenStock() throws Exception {
        ProductoSimple prod = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Alfajor Manual " + UUID.randomUUID())
                .precio(new BigDecimal("5000")).stock(0).activo(true).build());

        int n = 8;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CyclicBarrier barrera = new CyclicBarrier(n);
        List<Future<?>> futuros = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            futuros.add(pool.submit(() -> {
                RegistrarMovimientoDTO dto = new RegistrarMovimientoDTO();
                dto.setTipoItem(TipoItemStock.PRODUCTO);
                dto.setItemId(prod.getId());
                dto.setTipoMovimiento(TipoMovimientoStock.ENTRADA_COMPRA);
                dto.setCantidad(1.0);
                barrera.await();
                inventarioService.registrarMovimientoManual(dto, "test");
                return null;
            }));
        }
        for (Future<?> f : futuros) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(8, productoSimpleRepository.findById(prod.getId()).orElseThrow().getStock());
    }
}
