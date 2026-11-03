package com.heladeria.api.services;

import com.heladeria.api.dto.DetallePedidoRequestDTO;
import com.heladeria.api.dto.PedidoRequestDTO;
import com.heladeria.api.dto.ResumenCajaDTO;
import com.heladeria.api.entities.ProductoSimple;
import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.repositories.ProductoSimpleRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
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

/** Una venta a crédito no cobró nada: no puede inflar el efectivo esperado del arqueo. */
@SpringBootTest
@Transactional
@DisplayName("Caja: ventas a crédito")
class CajaCreditoTest {

    @Autowired private PedidoService pedidoService;
    @Autowired private CajaService cajaService;
    @Autowired private ProductoSimpleRepository productoSimpleRepository;
    @Autowired private SesionCajaRepository sesionCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private ProductoSimple producto;
    private SesionCaja sesion;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_CAJERO"), new SimpleGrantedAuthority("ROLE_ADMIN"))));
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        sesion = sesionCajaRepository.save(SesionCaja.builder().usuarioApertura(admin).fechaApertura(LocalDateTime.now())
                .montoInicial(new BigDecimal("100000")).estado(EstadoSesionCaja.ABIERTA).build());
        producto = productoSimpleRepository.save(ProductoSimple.builder().nombre("Alfajor Cred " + UUID.randomUUID().toString().substring(0, 8))
                .precio(new BigDecimal("10000")).stock(20).activo(true).build());
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void vender(int cantidad, CondicionVenta condicion) {
        DetallePedidoRequestDTO item = new DetallePedidoRequestDTO();
        item.setTipoItem(TipoItemPedido.PRODUCTO_SIMPLE);
        item.setProductoSimpleId(producto.getId());
        item.setCantidad(cantidad);
        PedidoRequestDTO req = new PedidoRequestDTO();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setCondicionVenta(condicion);
        req.setTipoEntrega(TipoEntrega.MOSTRADOR);
        req.setItems(List.of(item));
        pedidoService.crearPedido(req);
    }

    @Test
    @DisplayName("El efectivo esperado solo incluye lo cobrado; la venta a crédito figura aparte")
    void creditoNoEsEfectivo() {
        vender(2, CondicionVenta.CONTADO);   // 20.000 cobrados
        vender(3, CondicionVenta.CREDITO);   // 30.000 por cobrar

        ResumenCajaDTO r = cajaService.obtenerResumenActual();

        assertEquals(0, new BigDecimal("50000").compareTo(r.getTotalVentas()), "las ventas incluyen el crédito");
        assertEquals(0, new BigDecimal("20000").compareTo(r.getTotalEfectivo()), "pero el efectivo no");
        assertEquals(0, new BigDecimal("30000").compareTo(r.getTotalVentasCredito()));
        assertEquals(0, new BigDecimal("120000").compareTo(r.getTotalEsperadoEfectivo()), "100.000 iniciales + 20.000 cobrados");
    }
}
