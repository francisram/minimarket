package com.heladeria.api.services;

import com.heladeria.api.dto.AbrirCajaRequestDTO;
import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.dto.PuntoExpedicionDTO;
import com.heladeria.api.dto.PuntoExpedicionRequestDTO;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.PuntoExpedicion;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.PuntoExpedicionRepository;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PuntoExpedicionServiceTest {

    @Autowired
    private PuntoExpedicionService puntoExpedicionService;

    @Autowired
    private PuntoExpedicionRepository puntoExpedicionRepository;

    @Autowired
    private CajaService cajaService;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoSimpleRepository productoSimpleRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario cajero1;
    private Usuario cajero2;
    private ProductoSimple productoTest;

    @BeforeEach
    void setUp() {
        sesionCajaRepository.deleteAll();
        pedidoRepository.deleteAll();
        puntoExpedicionRepository.deleteAll();
        puntoExpedicionRepository.flush();

        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });

        cajero1 = usuarioRepository.findByUsername("cajero_test_1").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cajero_test_1");
            u.setPasswordHash("pass");
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        cajero2 = usuarioRepository.findByUsername("cajero_test_2").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cajero_test_2");
            u.setPasswordHash("pass");
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        productoTest = productoSimpleRepository.findAll().stream().findFirst().orElseGet(() -> {
            ProductoSimple p = new ProductoSimple();
            p.setNombre("Agua Mineral Test");
            p.setPrecio(new BigDecimal("5000"));
            p.setActivo(true);
            p.setTipoIva(TipoIva.IVA_10);
            return productoSimpleRepository.save(p);
        });
    }

    @Test
    @DisplayName("Crear y listar puntos de expedición con validaciones fiscales")
    void testCrearYListarPuntosExpedicion() {
        PuntoExpedicionRequestDTO req1 = new PuntoExpedicionRequestDTO(
                "Caja 1 Mostrador", "001", "001", "12345678",
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1),
                1L, 1000L, 0L, true
        );
        PuntoExpedicionDTO creado1 = puntoExpedicionService.crear(req1, "admin");
        assertNotNull(creado1.id());
        assertEquals("001-001", creado1.prefijoLegal());
        assertEquals("001-001-0000001", creado1.proximaFactura());
        assertFalse(creado1.enUso());

        // Intentar crear duplicado del mismo establecimiento y punto
        PuntoExpedicionRequestDTO reqDuplicado = new PuntoExpedicionRequestDTO(
                "Caja 1 Clon", "001", "001", "12345678",
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1),
                1L, 1000L, 0L, true
        );
        assertThrows(ReglaDeNegocioException.class, () -> puntoExpedicionService.crear(reqDuplicado, "admin"));

        // Crear Caja 2
        PuntoExpedicionRequestDTO req2 = new PuntoExpedicionRequestDTO(
                "Caja 2 Take Away", "001", "002", "12345678",
                LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1),
                1L, 1000L, 0L, true
        );
        PuntoExpedicionDTO creado2 = puntoExpedicionService.crear(req2, "admin");
        assertEquals("001-002", creado2.prefijoLegal());

        List<PuntoExpedicionDTO> disponibles = puntoExpedicionService.listarDisponibles();
        assertEquals(2, disponibles.size());
    }

    @Test
    @DisplayName("Cajas simultáneas en puntos distintos: apertura y facturación concurrente sin colisiones")
    void testCajasSimultaneasPuntosDistintos() {
        PuntoExpedicion p1 = puntoExpedicionRepository.save(PuntoExpedicion.builder()
                .nombre("Caja 1")
                .establecimiento("001")
                .puntoEmision("001")
                .timbrado("17894561")
                .timbradoVencimiento(LocalDate.now().plusYears(1))
                .numeroInicioFactura(1L)
                .numeroFinFactura(1000L)
                .ultimoNumeroFactura(0L)
                .activo(true)
                .build());

        PuntoExpedicion p2 = puntoExpedicionRepository.save(PuntoExpedicion.builder()
                .nombre("Caja 2")
                .establecimiento("001")
                .puntoEmision("002")
                .timbrado("17894561")
                .timbradoVencimiento(LocalDate.now().plusYears(1))
                .numeroInicioFactura(1L)
                .numeroFinFactura(1000L)
                .ultimoNumeroFactura(0L)
                .activo(true)
                .build());

        // Cajero 1 abre Caja 1
        cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("100000"), "Turno 1", p1.getId()), cajero1.getUsername());

        // Cajero 2 abre Caja 2
        cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("150000"), "Turno 2", p2.getId()), cajero2.getUsername());

        // Verificar que disponibles ahora es 0
        List<PuntoExpedicionDTO> disponibles = puntoExpedicionService.listarDisponibles();
        assertEquals(0, disponibles.size());

        // Verificar que listar todos muestra ambos enUso = true
        List<PuntoExpedicionDTO> todos = puntoExpedicionService.listarTodos();
        assertEquals(2, todos.size());
        assertTrue(todos.stream().allMatch(PuntoExpedicionDTO::enUso));

        // Facturar en Caja 1
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cajero1.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO")))
        );

        PedidoRequestDTO pedidoCajero1 = new PedidoRequestDTO();
        pedidoCajero1.setTipoComprobante(TipoComprobante.FACTURA);
        pedidoCajero1.setCondicionVenta(CondicionVenta.CONTADO);
        pedidoCajero1.setClienteRuc("80106020-6");
        pedidoCajero1.setClienteNombre("Empresa Uno S.A.");
        pedidoCajero1.setMetodoPago(MetodoPago.EFECTIVO);
        pedidoCajero1.setTipoEntrega(TipoEntrega.MOSTRADOR);
        DetallePedidoRequestDTO d1 = new DetallePedidoRequestDTO();
        d1.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        d1.setProductoSimpleId(productoTest.getId());
        d1.setCantidad(2);
        pedidoCajero1.setItems(List.of(d1));

        Pedido res1 = pedidoService.crearPedido(pedidoCajero1, cajero1.getUsername());
        assertNotNull(res1.getNumeroFactura());
        assertEquals("001-001-0000001", res1.getNumeroFactura());
        assertEquals(p1.getId(), res1.getPuntoExpedicionId());

        // Facturar en Caja 2
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cajero2.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO")))
        );

        PedidoRequestDTO pedidoCajero2 = new PedidoRequestDTO();
        pedidoCajero2.setTipoComprobante(TipoComprobante.FACTURA);
        pedidoCajero2.setCondicionVenta(CondicionVenta.CONTADO);
        pedidoCajero2.setClienteRuc("3669922-5");
        pedidoCajero2.setClienteNombre("Juan Pérez");
        pedidoCajero2.setMetodoPago(MetodoPago.EFECTIVO);
        pedidoCajero2.setTipoEntrega(TipoEntrega.MOSTRADOR);
        DetallePedidoRequestDTO d2 = new DetallePedidoRequestDTO();
        d2.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        d2.setProductoSimpleId(productoTest.getId());
        d2.setCantidad(1);
        pedidoCajero2.setItems(List.of(d2));

        Pedido res2 = pedidoService.crearPedido(pedidoCajero2, cajero2.getUsername());
        assertNotNull(res2.getNumeroFactura());
        // Caja 2 emite su propio correlativo independiente 001-002-0000001
        assertEquals("001-002-0000001", res2.getNumeroFactura());
        assertEquals(p2.getId(), res2.getPuntoExpedicionId());

        // Facturar nuevamente en Caja 1 para verificar correlativo 001-001-0000002
        Pedido res3 = pedidoService.crearPedido(pedidoCajero1, cajero1.getUsername());
        assertEquals("001-001-0000002", res3.getNumeroFactura());
    }

    @Test
    @DisplayName("Conflicto al intentar abrir el mismo punto de expedición en dos cajas simultáneas")
    void testConflictoMismoPuntoCajaSimultanea() {
        PuntoExpedicion p1 = puntoExpedicionRepository.save(PuntoExpedicion.builder()
                .nombre("Caja 1")
                .establecimiento("001")
                .puntoEmision("001")
                .timbrado("17894561")
                .timbradoVencimiento(LocalDate.now().plusYears(1))
                .numeroInicioFactura(1L)
                .numeroFinFactura(1000L)
                .ultimoNumeroFactura(0L)
                .activo(true)
                .build());

        // Cajero 1 abre Caja 1 con punto p1
        cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("100000"), "Turno 1", p1.getId()), cajero1.getUsername());

        // Cajero 2 intenta abrir también con punto p1
        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () ->
                cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Turno 2", p1.getId()), cajero2.getUsername())
        );
        assertTrue(ex.getMessage().contains("ya está siendo utilizado"));
    }

    @Test
    @DisplayName("No se puede desactivar un punto de expedición si está en uso por una caja abierta")
    void testNoDesactivarPuntoEnUso() {
        PuntoExpedicion p1 = puntoExpedicionRepository.save(PuntoExpedicion.builder()
                .nombre("Caja 1")
                .establecimiento("001")
                .puntoEmision("001")
                .timbrado("17894561")
                .timbradoVencimiento(LocalDate.now().plusYears(1))
                .numeroInicioFactura(1L)
                .numeroFinFactura(1000L)
                .ultimoNumeroFactura(0L)
                .activo(true)
                .build());

        cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("100000"), "Turno 1", p1.getId()), cajero1.getUsername());

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () ->
                puntoExpedicionService.cambiarEstado(p1.getId(), false, "admin")
        );
        assertTrue(ex.getMessage().contains("en uso"));
    }
}
