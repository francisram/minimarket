package com.heladeria.api.services;

import com.heladeria.api.dto.*;
import com.heladeria.api.dto.reportes.LibroVentasIvaDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Notas de crédito")
class NotaCreditoServiceTest {

    @Autowired private NotaCreditoService notaCreditoService;
    @Autowired private PedidoService pedidoService;
    @Autowired private ReporteService reporteService;
    @Autowired private ProductoSimpleRepository productoSimpleRepository;
    @Autowired private SesionCajaRepository sesionCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private MovimientoCajaRepository movimientoCajaRepository;
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;

    private ProductoSimple producto;
    private Pedido factura;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"), new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        sesionCajaRepository.save(SesionCaja.builder().usuarioApertura(admin).fechaApertura(LocalDateTime.now())
                .montoInicial(new BigDecimal("500000")).estado(EstadoSesionCaja.ABIERTA).build());

        producto = productoSimpleRepository.save(ProductoSimple.builder().nombre("Alfajor NC " + UUID.randomUUID().toString().substring(0, 8))
                .precio(new BigDecimal("10000")).stock(10).activo(true).tipoIva(TipoIva.IVA_10).build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(producto.getId());
        item.setCantidad(3);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setTipoComprobante(TipoComprobante.FACTURA);
        req.setClienteRuc("80012345-0");
        req.setClienteNombre("Cliente NC");
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));
        factura = pedidoService.crearPedido(req);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void cargarSerie() {
        SerieNotaCreditoDTO serie = new SerieNotaCreditoDTO();
        serie.timbrado = "55512345";
        serie.timbradoVencimiento = LocalDate.now().plusMonths(6);
        serie.numeroInicio = 1L;
        serie.numeroFin = 100L;
        notaCreditoService.guardarSerie(factura.getPuntoExpedicion().getId(), serie);
    }

    private NotaCreditoRequestDTO parcial(int cantidad, boolean reintegra) {
        NotaCreditoRequestDTO r = new NotaCreditoRequestDTO();
        r.setMotivo("Devolución del cliente");
        r.setReintegrarStock(reintegra);
        NotaCreditoRequestDTO.Linea l = new NotaCreditoRequestDTO.Linea();
        l.setDetalleId(factura.getDetalles().get(0).getId());
        l.setCantidad(cantidad);
        r.setItems(List.of(l));
        return r;
    }

    private int stock() {
        em.flush();
        em.clear();
        return productoSimpleRepository.findById(producto.getId()).orElseThrow().getStock();
    }

    @Test
    @DisplayName("Una nota de crédito parcial acredita lo devuelto, reintegra el stock y devuelve el efectivo de la caja")
    void parcial() {
        cargarSerie();
        assertEquals(7, stock());

        NotaCreditoDTO nc = notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin");

        assertEquals("001-001-0000001", nc.numero);
        assertEquals("55512345", nc.timbrado);
        assertEquals(0, new BigDecimal("10000").compareTo(nc.total));
        assertEquals(0, new BigDecimal("909").compareTo(nc.totalIva10), "IVA 10% incluido en 10.000 = 909");
        assertEquals(8, stock(), "vuelve 1 al inventario");
        assertTrue(nc.egresoCajaRegistrado);
        assertEquals(1, movimientoCajaRepository.findAll().stream()
                .filter(m -> m.getCategoria() == CategoriaMovimientoCaja.DEVOLUCION_CLIENTE).count());
    }

    @Test
    @DisplayName("Sin reintegro de stock la mercadería no vuelve al inventario")
    void sinReintegro() {
        cargarSerie();
        notaCreditoService.emitir(factura.getId(), parcial(1, false), "admin");
        assertEquals(7, stock());
    }

    @Test
    @DisplayName("Las notas parciales suman exactamente la factura y no se puede acreditar de más")
    void noSeAcreditaDeMas() {
        cargarSerie();
        BigDecimal total = BigDecimal.ZERO;
        total = total.add(notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin").total);
        total = total.add(notaCreditoService.emitir(factura.getId(), parcial(2, true), "admin").total);
        assertEquals(0, factura.getTotal().compareTo(total), "la suma de las notas es el total de la factura");

        assertThrows(ReglaDeNegocioException.class, () -> notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin"));
        NotaCreditoRequestDTO total2 = new NotaCreditoRequestDTO();
        total2.setMotivo("x");
        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class, () -> notaCreditoService.emitir(factura.getId(), total2, "admin"));
        assertTrue(e.getMessage().contains("acreditada por completo"), e.getMessage());
    }

    @Test
    @DisplayName("Sin items se acredita toda la factura, con numeración correlativa")
    void totalYCorrelativo() {
        cargarSerie();
        notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin");
        NotaCreditoRequestDTO resto = new NotaCreditoRequestDTO();
        resto.setMotivo("Anulación del resto");
        NotaCreditoDTO nc2 = notaCreditoService.emitir(factura.getId(), resto, "admin");

        assertEquals("001-001-0000002", nc2.numero);
        assertEquals(0, new BigDecimal("20000").compareTo(nc2.total));
        assertEquals(10, stock());
    }

    @Test
    @DisplayName("Sin timbrado de notas de crédito cargado no se puede emitir")
    void sinSerie() {
        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class,
                () -> notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin"));
        assertTrue(e.getMessage().contains("timbrado de notas de crédito"), e.getMessage());
    }

    @Test
    @DisplayName("Una factura anulada o un ticket no admiten nota de crédito")
    void soloFacturasVigentes() {
        cargarSerie();
        pedidoService.cancelarPedido(factura.getId());
        assertThrows(ReglaDeNegocioException.class, () -> notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin"));
    }

    @Test
    @DisplayName("Un cajero sin la clave de un administrador no puede emitirla")
    void cajeroNecesitaAutorizacion() {
        cargarSerie();
        Usuario cajero = new Usuario();
        cajero.setUsername("cajero_nc_" + UUID.randomUUID().toString().substring(0, 6));
        cajero.setPasswordHash("x");
        cajero.setRol(rolRepository.findByNombreRol("CAJERO").orElseThrow());
        cajero.setEstado(true);
        usuarioRepository.save(cajero);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                cajero.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"))));

        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class,
                () -> notaCreditoService.emitir(factura.getId(), parcial(1, true), cajero.getUsername()));
        assertTrue(e.getMessage().contains("contraseña de un Administrador"), e.getMessage());
    }

    @Test
    @DisplayName("El libro IVA resta las notas de crédito (importes negativos)")
    void libroIvaResta() {
        cargarSerie();
        notaCreditoService.emitir(factura.getId(), parcial(1, true), "admin");

        LibroVentasIvaDTO libro = reporteService.obtenerLibroVentasIva(LocalDate.now(), LocalDate.now());

        var nc = libro.getItems().stream().filter(i -> i.getTipoComprobante() == TipoComprobante.NOTA_CREDITO).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-10000").compareTo(nc.getTotal()));
        assertEquals(0, new BigDecimal("-909").compareTo(nc.getIva10()));
        assertEquals(0, new BigDecimal("20000").compareTo(libro.getGranTotal()), "30.000 de la factura menos 10.000 de la nota");
    }
}
