package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.CondicionVenta;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoComprobante;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.entities.enums.TipoItemPedido;
import com.heladeria.api.entities.enums.TipoIva;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private SaborRepository saborRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    @Autowired
    private ProductoSimpleRepository productoSimpleRepository;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstitucionRepository institucionRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Presentacion poteMedioKg;
    private Sabor dulceDeLeche;
    private Sabor frutilla;
    private Sabor chocolateAmargo;
    private Sabor saborAgotado;
    private Topping banioChocolate;

    @BeforeEach
    void setUp() {
        Usuario usuarioTest = usuarioRepository.findByUsername("admin").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("admin_test");
            u.setPasswordHash("hash");
            return usuarioRepository.save(u);
        });

        sesionCajaRepository.save(SesionCaja.builder()
                .usuarioApertura(usuarioTest)
                .fechaApertura(java.time.LocalDateTime.now())
                .montoInicial(new BigDecimal("50000"))
                .estado(com.heladeria.api.entities.enums.EstadoSesionCaja.ABIERTA)
                .build());

        poteMedioKg = presentacionRepository.save(Presentacion.builder()
                .nombre("Pote Test 1/2 Kg")
                .precio(new BigDecimal("35000.00"))
                .maxSabores(2) // Permite solo 2 sabores
                .activo(true)
                .build());

        dulceDeLeche = saborRepository.save(Sabor.builder()
                .nombre("DDL Test")
                .categoria(CategoriaSabor.DULCE_DE_LECHE)
                .disponible(true)
                .build());

        frutilla = saborRepository.save(Sabor.builder()
                .nombre("Frutilla Test")
                .categoria(CategoriaSabor.FRUTAL)
                .disponible(true)
                .build());

        chocolateAmargo = saborRepository.save(Sabor.builder()
                .nombre("Chocolate Test")
                .categoria(CategoriaSabor.CHOCOLATE)
                .disponible(true)
                .build());

        saborAgotado = saborRepository.save(Sabor.builder()
                .nombre("Sabor Agotado Test")
                .categoria(CategoriaSabor.CREMA)
                .disponible(false)
                .build());

        banioChocolate = toppingRepository.save(Topping.builder()
                .nombre("Baño Choco Test")
                .precioExtra(new BigDecimal("3000.00"))
                .disponible(true)
                .build());
    }

    @Test
    @DisplayName("Debe procesar exitosamente un pedido de helado con sabores y topping")
    void testCrearPedidoHeladoExitoso() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId(), frutilla.getId()));
        itemHelado.setToppingIds(List.of(banioChocolate.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteNombre("Carlos Gomez");
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        Pedido pedido = pedidoService.crearPedido(request);

        assertNotNull(pedido.getId());
        assertEquals(1, pedido.getDetalles().size());
        // Precio esperado: 35000 (helado) + 3000 (topping) = 38000
        assertEquals(0, new BigDecimal("38000.00").compareTo(pedido.getTotal()));
        assertEquals(2, pedido.getDetalles().get(0).getSabores().size());
        assertEquals(1, pedido.getDetalles().get(0).getToppings().size());
    }

    @Test
    @DisplayName("Debe fallar si se eligen más sabores que el máximo permitido por la presentación")
    void testExcesoDeSaboresFalla() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId()); // maxSabores = 2
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId(), frutilla.getId(), chocolateAmargo.getId())); // 3 sabores
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class, () -> {
            pedidoService.crearPedido(request);
        });

        assertTrue(exception.getMessage().contains("permite un máximo de 2 sabores"));
    }

    @Test
    @DisplayName("Debe fallar si alguno de los sabores seleccionados no está disponible")
    void testSaborNoDisponibleFalla() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId(), saborAgotado.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class, () -> {
            pedidoService.crearPedido(request);
        });

        assertTrue(exception.getMessage().contains("no se encuentra disponible"));
    }

    @Test
    @DisplayName("Debe procesar producto simple y descontar el stock correspondiente")
    void testProductoSimpleConDescuentoDeStock() {
        ProductoSimple agua = productoSimpleRepository.save(ProductoSimple.builder()
                .nombre("Agua Test")
                .precio(new BigDecimal("5000.00"))
                .stock(10)
                .activo(true)
                .build());

        DetallePedidoRequestDTO itemAgua = new DetallePedidoRequestDTO();
        itemAgua.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        itemAgua.setProductoSimpleId(agua.getId());
        itemAgua.setCantidad(3);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.TRANSFERENCIA_QR);
        request.setTipoEntrega(TipoEntrega.TAKE_AWAY);
        request.setItems(List.of(itemAgua));

        Pedido pedido = pedidoService.crearPedido(request);

        assertNotNull(pedido.getId());
        assertEquals(0, new BigDecimal("15000.00").compareTo(pedido.getTotal()));

        // Verificar que el stock se descontó de 10 a 7
        ProductoSimple aguaActualizada = productoSimpleRepository.findById(agua.getId()).orElseThrow();
        assertEquals(7, aguaActualizada.getStock());
    }

    @Test
    @DisplayName("Debe descontar stock de envase/cucurucho y kilos de helado equitativamente entre los sabores")
    void testVentaHeladoConDescuentoDeStockEnvaseYKilos() {
        Presentacion pote500 = presentacionRepository.save(Presentacion.builder()
                .nombre("Pote Stock 500g")
                .precio(new BigDecimal("35000.00"))
                .maxSabores(2)
                .pesoGramosAprox(500)
                .stock(10)
                .stockMinimo(2)
                .activo(true)
                .build());

        Sabor sab1 = saborRepository.save(Sabor.builder()
                .nombre("Sabor Stock 1")
                .categoria(CategoriaSabor.CREMA)
                .disponible(true)
                .stockKilos(5.0)
                .stockMinimoKilos(1.0)
                .build());

        Sabor sab2 = saborRepository.save(Sabor.builder()
                .nombre("Sabor Stock 2")
                .categoria(CategoriaSabor.FRUTAL)
                .disponible(true)
                .stockKilos(3.0)
                .stockMinimoKilos(1.0)
                .build());

        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(pote500.getId());
        itemHelado.setSaborIds(List.of(sab1.getId(), sab2.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        Pedido pedido = pedidoService.crearPedido(request);
        assertNotNull(pedido.getId());

        // Verificar descuento de envase: de 10 a 9
        Presentacion presActualizada = presentacionRepository.findById(pote500.getId()).orElseThrow();
        assertEquals(9, presActualizada.getStock());

        // 500g entre 2 sabores = 250g (0.25 kg) por sabor
        Sabor sab1Act = saborRepository.findById(sab1.getId()).orElseThrow();
        assertEquals(4.75, sab1Act.getStockKilos(), 0.001);

        Sabor sab2Act = saborRepository.findById(sab2.getId()).orElseThrow();
        assertEquals(2.75, sab2Act.getStockKilos(), 0.001);
    }

    @Test
    @DisplayName("Debe fallar si no hay suficiente stock del envase seleccionado")
    void testVentaHeladoFallaPorStockInsuficienteDeEnvase() {
        Presentacion cucurucho = presentacionRepository.save(Presentacion.builder()
                .nombre("Cucurucho Sin Stock")
                .precio(new BigDecimal("12000.00"))
                .maxSabores(1)
                .pesoGramosAprox(100)
                .stock(1) // Solo queda 1
                .activo(true)
                .build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.HELADO);
        item.setPresentacionId(cucurucho.getId());
        item.setSaborIds(List.of(dulceDeLeche.getId()));
        item.setCantidad(2); // Se piden 2

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(item));

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(request));
        assertTrue(ex.getMessage().contains("Stock insuficiente de envases"));
    }

    @Test
    @DisplayName("Debe fallar si no hay suficientes kilos del sabor seleccionado")
    void testVentaHeladoFallaPorStockInsuficienteDeSabor() {
        Presentacion pote1Kg = presentacionRepository.save(Presentacion.builder()
                .nombre("Pote 1 Kg Test")
                .precio(new BigDecimal("60000.00"))
                .maxSabores(1)
                .pesoGramosAprox(1000)
                .stock(20)
                .activo(true)
                .build());

        Sabor saborPocoStock = saborRepository.save(Sabor.builder()
                .nombre("Sabor Escaso")
                .categoria(CategoriaSabor.ESPECIAL)
                .disponible(true)
                .stockKilos(0.4) // Solo 400g disponibles
                .build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.HELADO);
        item.setPresentacionId(pote1Kg.getId());
        item.setSaborIds(List.of(saborPocoStock.getId())); // Requiere 1000g (1.0 kg)
        item.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(item));

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(request));
        assertTrue(ex.getMessage().contains("Stock insuficiente de helado para el sabor"));
    }

    @Test
    @DisplayName("Debe restaurar stock de envases y kilos de sabores al cancelar pedido de helado")
    void testCancelarPedidoRestauraStockEnvaseYSabores() {
        Presentacion vaso = presentacionRepository.save(Presentacion.builder()
                .nombre("Vaso Test Stock")
                .precio(new BigDecimal("15000.00"))
                .maxSabores(1)
                .pesoGramosAprox(200)
                .stock(15)
                .activo(true)
                .build());

        Sabor menta = saborRepository.save(Sabor.builder()
                .nombre("Menta Test Stock")
                .categoria(CategoriaSabor.CREMA)
                .disponible(true)
                .stockKilos(2.0)
                .build());

        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.HELADO);
        item.setPresentacionId(vaso.getId());
        item.setSaborIds(List.of(menta.getId()));
        item.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(item));

        Pedido pedido = pedidoService.crearPedido(request);

        // Verificar que se descontaron
        assertEquals(14, presentacionRepository.findById(vaso.getId()).orElseThrow().getStock());
        assertEquals(1.8, saborRepository.findById(menta.getId()).orElseThrow().getStockKilos(), 0.001);

        // Cancelar pedido
        pedidoService.cancelarPedido(pedido.getId());

        // Verificar restitución
        assertEquals(15, presentacionRepository.findById(vaso.getId()).orElseThrow().getStock());
        assertEquals(2.0, saborRepository.findById(menta.getId()).orElseThrow().getStockKilos(), 0.001);
    }

    @Test
    @DisplayName("Debe fallar al crear un pedido si no existe una sesión de caja abierta")
    void testCrearPedidoSinCajaAbiertaFalla() {
        sesionCajaRepository.deleteAll();

        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        ReglaDeNegocioException exception = assertThrows(ReglaDeNegocioException.class, () -> {
            pedidoService.crearPedido(request);
        });

        assertEquals("No se pueden registrar ventas: no existe una sesión de caja abierta.", exception.getMessage());
    }

    @Test
    @DisplayName("Debe emitir factura fiscal con correlativo SET de 3 bloques e incrementar contador en Institucion")
    void testCrearPedidoFacturaExitosoConCorrelativo() {
        Institucion inst = institucionRepository.findById(1L).orElseGet(() -> new Institucion(1L, "Heladería", null));
        inst.setUltimoNumeroFactura(0L);
        institucionRepository.save(inst);

        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setTipoComprobante(TipoComprobante.FACTURA);
        request.setCondicionVenta(CondicionVenta.CONTADO);
        request.setClienteRuc("80012345-6");
        request.setClienteNombre("Acme Corp S.A.");
        request.setClienteDireccion("Palma 543");
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        Pedido pedido = pedidoService.crearPedido(request);

        assertNotNull(pedido.getId());
        assertEquals(TipoComprobante.FACTURA, pedido.getTipoComprobante());
        assertEquals(CondicionVenta.CONTADO, pedido.getCondicionVenta());
        assertEquals("80012345-6", pedido.getClienteRuc());
        assertEquals("Acme Corp S.A.", pedido.getClienteNombre());
        assertEquals("Palma 543", pedido.getClienteDireccion());
        assertEquals("001-001-0000001", pedido.getNumeroFactura());

        Institucion instActualizada = institucionRepository.findById(1L).orElseThrow();
        assertEquals(1L, instActualizada.getUltimoNumeroFactura());

        // Segunda factura consecutiva
        Pedido pedido2 = pedidoService.crearPedido(request);
        assertEquals("001-001-0000002", pedido2.getNumeroFactura());
        assertEquals(2L, institucionRepository.findById(1L).orElseThrow().getUltimoNumeroFactura());
    }

    @Test
    @DisplayName("Debe fallar al solicitar factura sin RUC o sin Razón Social")
    void testCrearPedidoFacturaSinRucOFalla() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO requestSinRuc = new PedidoRequestDTO();
        requestSinRuc.setTipoComprobante(TipoComprobante.FACTURA);
        requestSinRuc.setClienteNombre("Cliente Sin Ruc");
        requestSinRuc.setMetodoPago(MetodoPago.EFECTIVO);
        requestSinRuc.setTipoEntrega(TipoEntrega.MOSTRADOR);
        requestSinRuc.setItems(List.of(itemHelado));

        ReglaDeNegocioException ex1 = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(requestSinRuc));
        assertTrue(ex1.getMessage().contains("debe indicar el RUC y la Razón Social"));

        PedidoRequestDTO requestSinNombre = new PedidoRequestDTO();
        requestSinNombre.setTipoComprobante(TipoComprobante.FACTURA);
        requestSinNombre.setClienteRuc("80012345-6");
        requestSinNombre.setMetodoPago(MetodoPago.EFECTIVO);
        requestSinNombre.setTipoEntrega(TipoEntrega.MOSTRADOR);
        requestSinNombre.setItems(List.of(itemHelado));

        ReglaDeNegocioException ex2 = assertThrows(ReglaDeNegocioException.class, () -> pedidoService.crearPedido(requestSinNombre));
        assertTrue(ex2.getMessage().contains("debe indicar el RUC y la Razón Social"));
    }

    @Test
    @DisplayName("Debe emitir comprobante TICKET por defecto si no se especifica tipoComprobante")
    void testCrearPedidoTicketPorDefecto() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteNombre("Carlos Venta");
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        Pedido pedido = pedidoService.crearPedido(request);

        assertEquals(TipoComprobante.TICKET, pedido.getTipoComprobante());
        assertNull(pedido.getNumeroFactura());
    }

    @Test
    @DisplayName("Crear pedido con RUC autoguarda el cliente en la base de datos")
    void testCrearPedidoConRucGuardaClienteAutomaticamente() {
        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteRuc("7777777-7");
        request.setClienteNombre("Cliente Desde Pos");
        request.setClienteDireccion("Barrio Las Mercedes");
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado));

        pedidoService.crearPedido(request);

        assertTrue(clienteRepository.existsByRucIgnoreCase("7777777-7"));
        Cliente cliente = clienteRepository.findByRucIgnoreCase("7777777-7").orElseThrow();
        assertEquals("Cliente Desde Pos", cliente.getRazonSocial());
        assertEquals("Barrio Las Mercedes", cliente.getDireccion());
    }

    @Test
    @DisplayName("Crear pedido con productos a 10%, 5% y Exentas calcula y almacena correctamente la liquidación fiscal SET/DNIT")
    void testCrearPedidoConIvaConfigurableCalculaLiquidacionFiscalCorrectamente() {
        // 1. Helado al 10% (30.000 Gs.)
        poteMedioKg.setPrecio(new BigDecimal("30000"));
        poteMedioKg.setTipoIva(TipoIva.IVA_10);
        presentacionRepository.save(poteMedioKg);

        DetallePedidoRequestDTO itemHelado = new DetallePedidoRequestDTO();
        itemHelado.setTipoItem(TipoItemPedido.HELADO);
        itemHelado.setPresentacionId(poteMedioKg.getId());
        itemHelado.setSaborIds(List.of(dulceDeLeche.getId()));
        itemHelado.setCantidad(1);

        // 2. Producto Simple al 5% (21.000 Gs.)
        ProductoSimple prodIva5 = ProductoSimple.builder()
                .nombre("Yogur Artesanal")
                .precio(new BigDecimal("21000"))
                .tipoIva(TipoIva.IVA_5)
                .stock(10)
                .activo(true)
                .build();
        prodIva5 = productoSimpleRepository.save(prodIva5);

        DetallePedidoRequestDTO itemProd5 = new DetallePedidoRequestDTO();
        itemProd5.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        itemProd5.setProductoSimpleId(prodIva5.getId());
        itemProd5.setCantidad(1);

        // 3. Producto Simple Exento (15.000 Gs.)
        ProductoSimple prodExento = ProductoSimple.builder()
                .nombre("Libro Recetario Heladería")
                .precio(new BigDecimal("15000"))
                .tipoIva(TipoIva.EXENTA)
                .stock(10)
                .activo(true)
                .build();
        prodExento = productoSimpleRepository.save(prodExento);

        DetallePedidoRequestDTO itemProdExento = new DetallePedidoRequestDTO();
        itemProdExento.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        itemProdExento.setProductoSimpleId(prodExento.getId());
        itemProdExento.setCantidad(1);

        // Armar pedido
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteNombre("Contribuyente Mixto");
        request.setMetodoPago(MetodoPago.EFECTIVO);
        request.setTipoEntrega(TipoEntrega.MOSTRADOR);
        request.setItems(List.of(itemHelado, itemProd5, itemProdExento));

        Pedido pedido = pedidoService.crearPedido(request);

        assertNotNull(pedido.getId());
        assertEquals(new BigDecimal("66000"), pedido.getTotal());

        // Verificación de detalles y congelamiento de tipoIva
        assertEquals(3, pedido.getDetalles().size());
        assertEquals(TipoIva.IVA_10, pedido.getDetalles().get(0).getTipoIva());
        assertEquals(TipoIva.IVA_5, pedido.getDetalles().get(1).getTipoIva());
        assertEquals(TipoIva.EXENTA, pedido.getDetalles().get(2).getTipoIva());

        // Liquidación fiscal:
        // Exentas: 15.000
        assertEquals(new BigDecimal("15000"), pedido.getTotalExentas());

        // IVA 5%: 21.000 / 21 = 1.000; Gravada 5%: 20.000
        assertEquals(new BigDecimal("1000"), pedido.getTotalIva5());
        assertEquals(new BigDecimal("20000"), pedido.getTotalGravada5());

        // IVA 10%: 30.000 / 11 = 2.727; Gravada 10%: 27.273
        assertEquals(new BigDecimal("2727"), pedido.getTotalIva10());
        assertEquals(new BigDecimal("27273"), pedido.getTotalGravada10());

        // Total IVA: 1.000 + 2.727 = 3.727
        assertEquals(new BigDecimal("3727"), pedido.getTotalIva());

        // Cuadratura total: Exentas + Gravadas + IVA == Total
        BigDecimal sumaTotal = pedido.getTotalExentas()
                .add(pedido.getTotalGravada5())
                .add(pedido.getTotalIva5())
                .add(pedido.getTotalGravada10())
                .add(pedido.getTotalIva10());
        assertEquals(pedido.getTotal(), sumaTotal);
    }
}
