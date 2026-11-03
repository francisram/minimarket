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
class StockConcurrenciaIT {

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
    @Autowired SaborRepository saborRepository;
    @Autowired PresentacionRepository presentacionRepository;
    @Autowired ProductoRepository productoRepository;

    private List<String> crearCajeros(int n) {
        List<String> usernames = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Usuario u = new Usuario();
            u.setUsername("cajero_" + UUID.randomUUID().toString().substring(0, 8));
            u.setPasswordHash("hash");
            u = usuarioRepository.save(u);
            sesionCajaRepository.save(SesionCaja.builder()
                    .usuarioApertura(u)
                    .fechaApertura(LocalDateTime.now())
                    .montoInicial(new BigDecimal("50000"))
                    .estado(EstadoSesionCaja.ABIERTA)
                    .build());
            usernames.add(u.getUsername());
        }
        return usernames;
    }

    /** Lanza todas las ventas a la vez y devuelve {exitosas, rechazadasPorStock}. */
    private int[] venderEnParalelo(List<String> cajeros, PedidoRequestDTO request) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(cajeros.size());
        CyclicBarrier barrera = new CyclicBarrier(cajeros.size());
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger rechazadas = new AtomicInteger();
        List<Future<?>> futuros = new ArrayList<>();
        for (String cajero : cajeros) {
            futuros.add(pool.submit(() -> {
                try {
                    barrera.await();
                    request.setTrxId("TRX-" + UUID.randomUUID());
                    pedidoService.crearPedido(copiar(request), cajero);
                    ok.incrementAndGet();
                } catch (ReglaDeNegocioException e) {
                    assertTrue(e.getMessage().contains("Stock insuficiente"), e.getMessage());
                    rechazadas.incrementAndGet();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }));
        }
        for (Future<?> f : futuros) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();
        return new int[]{ok.get(), rechazadas.get()};
    }

    private PedidoRequestDTO copiar(PedidoRequestDTO o) {
        PedidoRequestDTO c = new PedidoRequestDTO();
        c.setClienteNombre(o.getClienteNombre());
        c.setMetodoPago(o.getMetodoPago());
        c.setTipoEntrega(o.getTipoEntrega());
        c.setItems(o.getItems());
        c.setTrxId("TRX-" + UUID.randomUUID());
        return c;
    }

    @Test
    @DisplayName("Ultima unidad de un producto simple: solo una venta concurrente debe prosperar")
    void ultimaUnidadProductoSimple() throws Exception {
        ProductoSimple prod = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Alfajor Concurrencia " + UUID.randomUUID())
                .precio(new BigDecimal("5000"))
                .stock(1)
                .activo(true)
                .build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(prod.getId());
        item.setCantidad(1);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setClienteNombre("Consumidor Final");
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));

        int[] r = venderEnParalelo(crearCajeros(8), req);

        assertEquals(1, r[0], "Solo una venta debe prosperar");
        assertEquals(7, r[1], "Las demás deben rechazarse por stock");
        assertEquals(0, productoSimpleRepository.findById(prod.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("Sabor con 1 kg y potes de 0.5 kg: exactamente 2 ventas de 5 deben prosperar y el stock nunca es negativo")
    void ultimosKilosDeSabor() throws Exception {
        Sabor sabor = saborRepository.save(Sabor.builder()
                .nombre("Sabor Concurrencia " + UUID.randomUUID())
                .categoria(CategoriaSabor.CREMA)
                .disponible(true)
                .stockKilos(1.0)
                .build());
        Presentacion pote = presentacionRepository.save(Presentacion.builder()
                .nombre("Pote 1/2 Concurrencia " + UUID.randomUUID())
                .precio(new BigDecimal("35000"))
                .maxSabores(2)
                .pesoGramosAprox(500)
                .activo(true)
                .build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.HELADO);
        item.setPresentacionId(pote.getId());
        item.setSaborIds(List.of(sabor.getId()));
        item.setCantidad(1);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setClienteNombre("Consumidor Final");
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));

        int[] r = venderEnParalelo(crearCajeros(5), req);

        assertEquals(2, r[0]);
        assertEquals(3, r[1]);
        Sabor finalSabor = saborRepository.findById(sabor.getId()).orElseThrow();
        assertEquals(0.0, finalSabor.getStockKilos(), 0.0005);
    }

    @Test
    @DisplayName("Combo con un componente de 1 unidad: solo una de 6 ventas concurrentes prospera y nada queda negativo")
    void ultimaUnidadDeComponenteDeCombo() throws Exception {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);
        ProductoSimple gaseosaOp = productoSimpleRepository.save(ProductoSimple.builder().nombre("Gaseosa " + sufijo)
                .precio(new BigDecimal("8000")).stock(1).activo(true).build());
        ProductoSimple comboVenta = productoSimpleRepository.save(ProductoSimple.builder().nombre("Combo " + sufijo)
                .precio(new BigDecimal("20000")).stock(0).activo(true).build());
        Producto gaseosaM = productoRepository.save(maestro("Gaseosa " + sufijo, TipoArticulo.VENTA_DIRECTA, 1));
        Producto combo = maestro("Combo " + sufijo, TipoArticulo.COMBO, 0);
        combo.agregarComponente(gaseosaM, BigDecimal.ONE, null);
        productoRepository.save(combo);

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(comboVenta.getId());
        item.setCantidad(1);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setClienteNombre("Consumidor Final");
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));

        int[] r = venderEnParalelo(crearCajeros(6), req);

        assertEquals(1, r[0], "Solo una venta debe prosperar");
        assertEquals(5, r[1]);
        assertEquals(0, productoSimpleRepository.findById(gaseosaOp.getId()).orElseThrow().getStock());
        assertEquals(0, BigDecimal.ZERO.compareTo(productoRepository.findById(gaseosaM.getId()).orElseThrow().getStock()));
    }

    private Producto maestro(String nombre, TipoArticulo tipo, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setTipoArticulo(tipo);
        p.setPrecioVenta(new BigDecimal("1000"));
        p.setStock(new BigDecimal(stock));
        p.setActivo(true);
        return p;
    }
}
