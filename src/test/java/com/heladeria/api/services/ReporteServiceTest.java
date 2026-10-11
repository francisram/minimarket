package com.heladeria.api.services;

import com.heladeria.api.dto.reportes.*;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Pruebas unitarias del servicio ReporteService")
class ReporteServiceTest {

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private SaborRepository saborRepository;

    @Autowired
    private ProductoSimpleRepository productoSimpleRepository;

    private Usuario cajeroUser;
    private SesionCaja sesion1;
    private SesionCaja sesion2;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();
        sesionCajaRepository.deleteAll();

        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });

        cajeroUser = usuarioRepository.findByUsername("cajero_reporte").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cajero_reporte");
            u.setPasswordHash("pass");
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        // Sesión 1: cerrada con sobrante de +5.000
        sesion1 = sesionCajaRepository.save(SesionCaja.builder()
                .usuarioApertura(cajeroUser)
                .usuarioCierre(cajeroUser)
                .fechaApertura(LocalDateTime.now().minusHours(5))
                .fechaCierre(LocalDateTime.now().minusHours(1))
                .montoInicial(new BigDecimal("50000"))
                .montoEsperadoEfectivo(new BigDecimal("80000"))
                .montoRealEfectivo(new BigDecimal("85000"))
                .diferencia(new BigDecimal("5000"))
                .totalVentasGeneral(new BigDecimal("30000"))
                .cantidadPedidos(1)
                .estado(EstadoSesionCaja.CERRADA)
                .build());

        // Sesión 2: cerrada con faltante de -2.000
        sesion2 = sesionCajaRepository.save(SesionCaja.builder()
                .usuarioApertura(cajeroUser)
                .usuarioCierre(cajeroUser)
                .fechaApertura(LocalDateTime.now().minusHours(8))
                .fechaCierre(LocalDateTime.now().minusHours(6))
                .montoInicial(new BigDecimal("50000"))
                .montoEsperadoEfectivo(new BigDecimal("60000"))
                .montoRealEfectivo(new BigDecimal("58000"))
                .diferencia(new BigDecimal("-2000"))
                .totalVentasGeneral(new BigDecimal("10000"))
                .cantidadPedidos(1)
                .estado(EstadoSesionCaja.CERRADA)
                .build());

        Presentacion pote = presentacionRepository.save(Presentacion.builder()
                .nombre("Pote 1/2 Kg Reporte")
                .precio(new BigDecimal("35000"))
                .pesoGramosAprox(500)
                .maxSabores(2)
                .activo(true)
                .tipoIva(TipoIva.IVA_10)
                .build());

        Sabor ddl = saborRepository.save(Sabor.builder()
                .nombre("Dulce de Leche Reporte")
                .categoria(CategoriaSabor.DULCE_DE_LECHE)
                .disponible(true)
                .build());

        ProductoSimple agua = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Agua Mineral Reporte")
                .precio(new BigDecimal("5000"))
                .activo(true)
                .tipoIva(TipoIva.IVA_5)
                .build());

        // Pedido 1: Efectivo, Factura, Entregado, Total 35.000 (IVA 10%: 3.182, Gravada 10%: 31.818)
        Pedido pedido1 = Pedido.builder()
                .sesionCaja(sesion1)
                .tipoComprobante(TipoComprobante.FACTURA)
                .condicionVenta(CondicionVenta.CONTADO)
                .clienteRuc("80012345-6")
                .clienteNombre("Empresa Test SA")
                .numeroFactura("001-001-0000001")
                .trxId("TRX-TEST-001")
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .fechaCreacion(LocalDateTime.now().minusHours(3))
                .total(new BigDecimal("35000"))
                .totalGravada10(new BigDecimal("31818"))
                .totalIva10(new BigDecimal("3182"))
                .totalIva(new BigDecimal("3182"))
                .detalles(new ArrayList<>())
                .build();

        DetallePedido det1 = DetallePedido.builder()
                .pedido(pedido1)
                .tipoItem(TipoItemPedido.HELADO)
                .presentacion(pote)
                .cantidad(1)
                .precioUnitario(new BigDecimal("35000"))
                .subtotal(new BigDecimal("35000"))
                .tipoIva(TipoIva.IVA_10)
                .sabores(List.of(ddl))
                .build();
        pedido1.getDetalles().add(det1);
        pedidoRepository.save(pedido1);

        // Pedido 2: Tarjeta Débito con Voucher, Ticket, Entregado, Total 10.000 (IVA 5%: 476, Gravada 5%: 9.524)
        Pedido pedido2 = Pedido.builder()
                .sesionCaja(sesion1)
                .tipoComprobante(TipoComprobante.TICKET)
                .condicionVenta(CondicionVenta.CONTADO)
                .clienteNombre("Juan Pérez")
                .numeroVoucher("VCH-999111")
                .trxId("TRX-TEST-002")
                .metodoPago(MetodoPago.TARJETA_DEBITO)
                .tipoEntrega(TipoEntrega.TAKE_AWAY)
                .estado(EstadoPedido.ENTREGADO)
                .fechaCreacion(LocalDateTime.now().minusHours(2))
                .total(new BigDecimal("10000"))
                .totalGravada5(new BigDecimal("9524"))
                .totalIva5(new BigDecimal("476"))
                .totalIva(new BigDecimal("476"))
                .detalles(new ArrayList<>())
                .build();

        DetallePedido det2 = DetallePedido.builder()
                .pedido(pedido2)
                .tipoItem(TipoItemPedido.PRODUCTO_SIMPLE)
                .productoSimple(agua)
                .cantidad(2)
                .precioUnitario(new BigDecimal("5000"))
                .subtotal(new BigDecimal("10000"))
                .tipoIva(TipoIva.IVA_5)
                .build();
        pedido2.getDetalles().add(det2);
        pedidoRepository.save(pedido2);

        // Pedido 3: CANCELADO (No debe computar en métricas de venta)
        Pedido pedidoCancelado = Pedido.builder()
                .sesionCaja(sesion1)
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.CANCELADO)
                .fechaCreacion(LocalDateTime.now().minusHours(1))
                .total(new BigDecimal("20000"))
                .detalles(new ArrayList<>())
                .build();
        pedidoRepository.save(pedidoCancelado);
    }

    @Test
    @DisplayName("Dashboard calcula correctamente KPIs ejecutivos y excluye pedidos cancelados")
    void testObtenerDashboard() {
        DashboardReporteDTO dashboard = reporteService.obtenerDashboard(LocalDate.now(), LocalDate.now());

        assertNotNull(dashboard);
        // Total ventas = 35.000 + 10.000 = 45.000 (el cancelado de 20.000 no suma)
        assertEquals(0, new BigDecimal("45000").compareTo(dashboard.getTotalVentas()));
        assertEquals(2, dashboard.getCantidadVentas());
        // Ticket promedio = 45.000 / 2 = 22.500
        assertEquals(0, new BigDecimal("22500").compareTo(dashboard.getTicketPromedio()));

        // Desglose por método de pago
        assertEquals(0, new BigDecimal("35000").compareTo(dashboard.getTotalEfectivo()));
        assertEquals(0, new BigDecimal("10000").compareTo(dashboard.getTotalTarjetaDebito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dashboard.getTotalTarjetaCredito()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dashboard.getTotalTransferenciaQr()));

        // Comprobantes
        assertEquals(1, dashboard.getTotalFacturas());
        assertEquals(1, dashboard.getTotalTickets());

        // Fiscal
        assertEquals(0, new BigDecimal("3658").compareTo(dashboard.getTotalIva())); // 3.182 + 476

        // Tops
        assertFalse(dashboard.getTopProductos().isEmpty());
        assertFalse(dashboard.getTopSabores().isEmpty());
        assertEquals("Dulce de Leche Reporte", dashboard.getTopSabores().get(0).getNombre());
    }

    @Test
    @DisplayName("Reporte detallado de ventas filtra correctamente por método de pago")
    void testObtenerReporteVentasConFiltroMetodoPago() {
        ReporteVentasDTO reporteVentas = reporteService.obtenerReporteVentas(
                LocalDate.now(), LocalDate.now(), MetodoPago.TARJETA_DEBITO, null, null, null);

        assertNotNull(reporteVentas);
        assertEquals(1, reporteVentas.getPedidos().size());
        PedidoResumenReporteDTO pedido = reporteVentas.getPedidos().get(0);
        assertEquals("VCH-999111", pedido.getNumeroVoucher());
        assertEquals("TRX-TEST-002", pedido.getTrxId());
        assertEquals(MetodoPago.TARJETA_DEBITO, pedido.getMetodoPago());
        assertEquals(0, new BigDecimal("10000").compareTo(pedido.getTotal()));
    }

    @Test
    @DisplayName("Reporte detallado de ventas filtra correctamente por trxId")
    void testObtenerReporteVentasConFiltroTrxId() {
        ReporteVentasDTO reporte = reporteService.obtenerReporteVentas(
                LocalDate.now(), LocalDate.now(), null, null, null, null, "TRX-TEST-001");

        assertNotNull(reporte);
        assertEquals(1, reporte.getPedidos().size());
        assertEquals("TRX-TEST-001", reporte.getPedidos().get(0).getTrxId());
        assertEquals("80012345-6", reporte.getPedidos().get(0).getClienteRuc());
    }

    @Test
    @DisplayName("Libro de Ventas Fiscal consolida totales y genera CSV con UTF-8 BOM")
    void testLibroVentasIvaYGeneracionCsv() {
        LibroVentasIvaDTO libro = reporteService.obtenerLibroVentasIva(LocalDate.now(), LocalDate.now());

        assertNotNull(libro);
        assertEquals(2, libro.getCantidadComprobantes());
        assertEquals(0, new BigDecimal("45000").compareTo(libro.getGranTotal()));
        assertEquals(0, new BigDecimal("31818").compareTo(libro.getTotalGravada10()));
        assertEquals(0, new BigDecimal("9524").compareTo(libro.getTotalGravada5()));

        byte[] csvBytes = reporteService.generarLibroVentasCsv(LocalDate.now(), LocalDate.now());
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 0);

        // Validar BOM UTF-8 (primeros 3 bytes: 0xEF, 0xBB, 0xBF)
        assertEquals((byte) 0xEF, csvBytes[0]);
        assertEquals((byte) 0xBB, csvBytes[1]);
        assertEquals((byte) 0xBF, csvBytes[2]);

        String csvString = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csvString.contains("Fecha;Tipo Comprobante;Condicion"));
        assertTrue(csvString.contains("Empresa Test SA"));
        assertTrue(csvString.contains("001-001-0000001"));
        assertTrue(csvString.contains("TOTALES"));
    }

    @Test
    @DisplayName("Reporte de cajas consolida sobrantes y faltantes de dinero")
    void testObtenerReporteCajas() {
        ReporteCajasDTO reporteCajas = reporteService.obtenerReporteCajas(LocalDate.now(), LocalDate.now(), null);

        assertNotNull(reporteCajas);
        assertEquals(2, reporteCajas.getTotalSesiones());
        assertEquals(0, new BigDecimal("40000").compareTo(reporteCajas.getTotalVentasGeneral()));
        assertEquals(0, new BigDecimal("5000").compareTo(reporteCajas.getTotalSobrante()));
        assertEquals(0, new BigDecimal("2000").compareTo(reporteCajas.getTotalFaltante()));
    }

    @Test
    @DisplayName("Ranking de productos reporta presentaciones, sabores y productos simples")
    void testObtenerRankingProductos() {
        RankingProductosDTO ranking = reporteService.obtenerRankingProductos(LocalDate.now(), LocalDate.now(), 5);

        assertNotNull(ranking);
        assertFalse(ranking.getTopPresentaciones().isEmpty());
        assertEquals("Pote 1/2 Kg Reporte", ranking.getTopPresentaciones().get(0).getNombre());

        assertFalse(ranking.getTopProductosSimples().isEmpty());
        assertEquals("Agua Mineral Reporte", ranking.getTopProductosSimples().get(0).getNombre());

        assertFalse(ranking.getTopSabores().isEmpty());
        assertEquals("Dulce de Leche Reporte", ranking.getTopSabores().get(0).getNombre());
    }
}
