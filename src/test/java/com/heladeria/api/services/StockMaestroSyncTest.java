package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** El stock del maestro de productos y el del catálogo operativo no deben desalinearse. */
@SpringBootTest
@Transactional
@DisplayName("Maestro de productos vs catálogo operativo: stock alineado")
class StockMaestroSyncTest {

    @Autowired private PedidoService pedidoService;
    @Autowired private ProductoService productoService;
    @Autowired private StockMaestroService stockMaestroService;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoSimpleRepository productoSimpleRepository;
    @Autowired private SesionCajaRepository sesionCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;

    private ProductoSimple operativo;
    private Producto maestro;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"), new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        sesionCajaRepository.save(SesionCaja.builder().usuarioApertura(admin).fechaApertura(LocalDateTime.now())
                .montoInicial(new BigDecimal("50000")).estado(EstadoSesionCaja.ABIERTA).build());

        String nombre = "Alfajor Sync " + UUID.randomUUID().toString().substring(0, 8);
        operativo = productoSimpleRepository.save(ProductoSimple.builder().nombre(nombre)
                .precio(new BigDecimal("5000")).stock(10).activo(true).build());
        Producto m = new Producto();
        m.setNombre(nombre);
        m.setTipoArticulo(TipoArticulo.VENTA_DIRECTA);
        m.setPrecioVenta(new BigDecimal("5000"));
        m.setStock(new BigDecimal("10"));
        m.setActivo(true);
        maestro = productoRepository.save(m);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private PedidoRequestDTO venta(int cantidad) {
        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(operativo.getId());
        item.setCantidad(cantidad);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));
        return req;
    }

    private int stockOperativo() {
        em.flush();
        em.clear(); // las actualizaciones atómicas no refrescan entidades ya cargadas
        return productoSimpleRepository.findById(operativo.getId()).orElseThrow().getStock();
    }

    private BigDecimal stockMaestro() {
        em.flush();
        em.clear();
        return productoRepository.findById(maestro.getId()).orElseThrow().getStock();
    }

    @Test
    @DisplayName("Vender y anular mueve el stock del operativo y del maestro por igual")
    void ventaYAnulacionEspejan() {
        Pedido p = pedidoService.crearPedido(venta(3));
        assertEquals(7, stockOperativo());
        assertEquals(0, new BigDecimal("7").compareTo(stockMaestro()));

        pedidoService.cancelarPedido(p.getId());
        assertEquals(10, stockOperativo());
        assertEquals(0, new BigDecimal("10").compareTo(stockMaestro()), "maestro=" + stockMaestro());
    }

    @Test
    @DisplayName("Editar datos del maestro (sin tocar el stock) no pisa el stock operativo")
    void editarMaestroNoResucitaStock() {
        pedidoService.crearPedido(venta(3));
        // El maestro quedó atrasado a propósito (como en bases anteriores a este arreglo)
        Producto m = productoRepository.findById(maestro.getId()).orElseThrow();
        m.setStock(new BigDecimal("10"));
        m.setPrecioVenta(new BigDecimal("6000"));
        productoRepository.saveAndFlush(m);

        productoService.sincronizarConCatalogoOperativo(m);

        assertEquals(7, stockOperativo());
    }

    @Test
    @DisplayName("Un ingreso de compra sobre el maestro suma al operativo en vez de reemplazarlo")
    void compraSumaAlOperativo() {
        pedidoService.crearPedido(venta(3)); // operativo 7
        productoRepository.aumentarStock(maestro.getId(), new BigDecimal("5"));

        stockMaestroService.aplicarEnOperativo(productoRepository.findById(maestro.getId()).orElseThrow(), new BigDecimal("5"));

        assertEquals(12, stockOperativo());
    }
}
