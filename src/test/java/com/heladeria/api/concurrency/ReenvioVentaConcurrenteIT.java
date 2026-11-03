package com.heladeria.api.concurrency;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import com.heladeria.api.services.PedidoService;
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
class ReenvioVentaConcurrenteIT {

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

    @Autowired PedidoService pedidoService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired SesionCajaRepository sesionCajaRepository;
    @Autowired ProductoSimpleRepository productoSimpleRepository;
    @Autowired PedidoRepository pedidoRepository;

    @Test
    @DisplayName("Dos envios simultaneos con el mismo trxId: una sola venta, un solo descuento de stock")
    void dobleEnvioSimultaneo() throws Exception {
        Usuario u = new Usuario();
        u.setUsername("cajero_" + UUID.randomUUID().toString().substring(0, 8));
        u.setPasswordHash("hash");
        u = usuarioRepository.save(u);
        sesionCajaRepository.save(SesionCaja.builder()
                .usuarioApertura(u).fechaApertura(LocalDateTime.now())
                .montoInicial(new BigDecimal("50000")).estado(EstadoSesionCaja.ABIERTA).build());
        String cajero = u.getUsername();

        ProductoSimple prod = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Alfajor Reenvio " + UUID.randomUUID())
                .precio(new BigDecimal("5000")).stock(5).activo(true).build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(prod.getId());
        item.setCantidad(1);
        String trx = "TRX-" + UUID.randomUUID();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrera = new CyclicBarrier(2);
        List<Future<Long>> futuros = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            futuros.add(pool.submit(() -> {
                PedidoRequestDTO req = new PedidoRequestDTO();
                req.setClienteNombre("Consumidor Final");
                req.setMetodoPago(MetodoPago.EFECTIVO);
                req.setTipoEntrega(TipoEntrega.MOSTRADOR);
                req.setItems(List.of(item));
                req.setTrxId(trx);
                barrera.await();
                try {
                    return pedidoService.crearPedido(req, cajero).getId();
                } catch (DataIntegrityViolationException e) {
                    // Es lo que hace PedidoController ante la carrera
                    return pedidoService.recuperarPorReenvio(trx, cajero).getId();
                }
            }));
        }
        Long id1 = futuros.get(0).get(60, TimeUnit.SECONDS);
        Long id2 = futuros.get(1).get(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(id1, id2);
        assertEquals(1, pedidoRepository.findAll().stream().filter(p -> trx.equals(p.getTrxId())).count());
        assertEquals(4, productoSimpleRepository.findById(prod.getId()).orElseThrow().getStock());
    }
}
