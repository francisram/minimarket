package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
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

import static org.junit.jupiter.api.Assertions.*;

/** Los componentes de un combo se descuentan de forma atómica y con validación de stock. */
@SpringBootTest
@Transactional
@DisplayName("Combos compuestos: descuento atómico y validación de stock")
class ComboStockTest {

    @Autowired private PedidoService pedidoService;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoSimpleRepository productoSimpleRepository;
    @Autowired private PresentacionRepository presentacionRepository;
    @Autowired private SesionCajaRepository sesionCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private String sufijo;
    private ProductoSimple comboVenta;
    private Producto gaseosaMaestro;
    private ProductoSimple gaseosaOperativa;
    private Producto vasoMaestro;
    private Presentacion vasoOperativo;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"))));
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        sesionCajaRepository.save(SesionCaja.builder().usuarioApertura(admin).fechaApertura(LocalDateTime.now())
                .montoInicial(new BigDecimal("50000")).estado(EstadoSesionCaja.ABIERTA).build());

        sufijo = UUID.randomUUID().toString().substring(0, 8);
        gaseosaOperativa = productoSimpleRepository.save(ProductoSimple.builder().nombre("Gaseosa " + sufijo)
                .precio(new BigDecimal("8000")).stock(5).activo(true).build());
        vasoOperativo = presentacionRepository.save(Presentacion.builder().nombre("Vaso " + sufijo)
                .precio(new BigDecimal("1000")).maxSabores(1).stock(5).activo(true).build());
        comboVenta = productoSimpleRepository.save(ProductoSimple.builder().nombre("Combo " + sufijo)
                .precio(new BigDecimal("20000")).stock(0).activo(true).build());

        gaseosaMaestro = maestro("Gaseosa " + sufijo, TipoArticulo.VENTA_DIRECTA, 5);
        vasoMaestro = maestro("Vaso " + sufijo, TipoArticulo.ENVASE, 5);
        Producto combo = maestro("Combo " + sufijo, TipoArticulo.COMBO, 0);
        combo.agregarComponente(gaseosaMaestro, BigDecimal.ONE, null);
        combo.agregarComponente(vasoMaestro, BigDecimal.ONE, null);
        productoRepository.save(combo);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private Producto maestro(String nombre, TipoArticulo tipo, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setTipoArticulo(tipo);
        p.setPrecioVenta(new BigDecimal("1000"));
        p.setStock(new BigDecimal(stock));
        p.setActivo(true);
        return productoRepository.save(p);
    }

    private PedidoRequestDTO ventaCombo(int cantidad) {
        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(comboVenta.getId());
        item.setCantidad(cantidad);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));
        return req;
    }

    @Test
    @DisplayName("Vender 2 combos descuenta 2 unidades de cada componente, en el maestro y en el catálogo operativo")
    void descuentaComponentes() {
        pedidoService.crearPedido(ventaCombo(2));

        assertEquals(0, new BigDecimal("3").compareTo(productoRepository.findById(gaseosaMaestro.getId()).orElseThrow().getStock()));
        assertEquals(0, new BigDecimal("3").compareTo(productoRepository.findById(vasoMaestro.getId()).orElseThrow().getStock()));
        assertEquals(3, productoSimpleRepository.findById(gaseosaOperativa.getId()).orElseThrow().getStock());
        assertEquals(3, presentacionRepository.findById(vasoOperativo.getId()).orElseThrow().getStock());
    }

    @Test
    @DisplayName("Si un componente del catálogo operativo no alcanza, la venta se rechaza")
    void rechazaPorStockOperativoInsuficiente() {
        ProductoSimple g = productoSimpleRepository.findById(gaseosaOperativa.getId()).orElseThrow();
        g.setStock(1);
        productoSimpleRepository.saveAndFlush(g);

        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(ventaCombo(2)));
        assertTrue(e.getMessage().contains("Stock insuficiente"), e.getMessage());
        assertTrue(e.getMessage().contains("Gaseosa " + sufijo), e.getMessage());
    }

    @Test
    @DisplayName("Si el maestro de productos del componente no alcanza, la venta se rechaza (antes quedaba en negativo)")
    void rechazaPorStockMaestroInsuficiente() {
        Producto m = productoRepository.findById(vasoMaestro.getId()).orElseThrow();
        m.setStock(BigDecimal.ONE);
        productoRepository.saveAndFlush(m);

        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(ventaCombo(2)));
        assertTrue(e.getMessage().contains("Stock insuficiente del componente 'Vaso " + sufijo + "'"), e.getMessage());
    }

    @Test
    @DisplayName("Anular una venta de combo devuelve los componentes al maestro y al catálogo operativo")
    void anularRestituyeComponentes() {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"), new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Pedido pedido = pedidoService.crearPedido(ventaCombo(2));
        assertEquals(3, productoSimpleRepository.findById(gaseosaOperativa.getId()).orElseThrow().getStock());

        pedidoService.cancelarPedido(pedido.getId());

        assertEquals(0, new BigDecimal("5").compareTo(productoRepository.findById(gaseosaMaestro.getId()).orElseThrow().getStock()));
        assertEquals(0, new BigDecimal("5").compareTo(productoRepository.findById(vasoMaestro.getId()).orElseThrow().getStock()));
        assertEquals(5, productoSimpleRepository.findById(gaseosaOperativa.getId()).orElseThrow().getStock());
        assertEquals(5, presentacionRepository.findById(vasoOperativo.getId()).orElseThrow().getStock());
    }
}
