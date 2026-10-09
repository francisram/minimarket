package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.CategoriaSabor;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.entities.enums.TipoItemPedido;
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

    private Presentacion poteMedioKg;
    private Sabor dulceDeLeche;
    private Sabor frutilla;
    private Sabor chocolateAmargo;
    private Sabor saborAgotado;
    private Topping banioChocolate;

    @BeforeEach
    void setUp() {
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
}
