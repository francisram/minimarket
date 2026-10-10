package com.heladeria.api.services;

import com.heladeria.api.dto.AbrirCajaRequestDTO;
import com.heladeria.api.dto.CerrarCajaRequestDTO;
import com.heladeria.api.dto.EstadoCajaDTO;
import com.heladeria.api.dto.ResumenCajaDTO;
import com.heladeria.api.dto.SesionCajaDTO;
import com.heladeria.api.entities.Pedido;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.SesionCaja;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.entities.enums.EstadoPedido;
import com.heladeria.api.entities.enums.EstadoSesionCaja;
import com.heladeria.api.entities.enums.MetodoPago;
import com.heladeria.api.entities.enums.TipoEntrega;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.PedidoRepository;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CajaServiceTest {

    @Autowired
    private CajaService cajaService;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Usuario usuarioCajero;
    private Usuario otroCajero;
    private Usuario usuarioAdmin;
    private Usuario usuarioOwner;

    @BeforeEach
    void setUp() {
        sesionCajaRepository.deleteAll();

        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });

        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("ADMIN");
            return rolRepository.save(r);
        });

        Rol rolOwner = rolRepository.findByNombreRol("OWNER").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("OWNER");
            return rolRepository.save(r);
        });

        usuarioCajero = usuarioRepository.findByUsername("cajero_test_caja").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cajero_test_caja");
            u.setPasswordHash("pass");
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        otroCajero = usuarioRepository.findByUsername("otro_cajero_test").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("otro_cajero_test");
            u.setPasswordHash("pass");
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        usuarioAdmin = usuarioRepository.findByUsername("admin_test_caja").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("admin_test_caja");
            u.setPasswordHash("pass");
            u.setRol(rolAdmin);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        usuarioOwner = usuarioRepository.findByUsername("owner_test_caja").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("owner_test_caja");
            u.setPasswordHash("pass");
            u.setRol(rolOwner);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });
    }

    @Test
    @DisplayName("Apertura de caja exitosa con fondo inicial en Gs.")
    void testAbrirCajaExitoso() {
        AbrirCajaRequestDTO request = new AbrirCajaRequestDTO(new BigDecimal("100000"), "Turno Mañana");

        SesionCajaDTO sesion = cajaService.abrirCaja(request, usuarioCajero.getUsername());

        assertNotNull(sesion.getId());
        assertEquals(EstadoSesionCaja.ABIERTA, sesion.getEstado());
        assertEquals(usuarioCajero.getUsername(), sesion.getUsuarioApertura());
        assertEquals(0, new BigDecimal("100000").compareTo(sesion.getMontoInicial()));
        assertEquals(0, new BigDecimal("0").compareTo(sesion.getTotalVentasGeneral()));
        assertEquals(0, sesion.getCantidadPedidos());
        assertNull(sesion.getFechaCierre());
        assertNull(sesion.getUsuarioCierre());

        EstadoCajaDTO estado = cajaService.obtenerEstado();
        assertTrue(estado.isAbierta());
        assertEquals(sesion.getId(), estado.getSesionId());
        assertEquals(usuarioCajero.getUsername(), estado.getCajeroApertura());
    }

    @Test
    @DisplayName("Falla al abrir caja si el usuario ya tiene una sesión abierta")
    void testAbrirCajaDuplicadaFalla() {
        cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), null), usuarioCajero.getUsername());

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> {
            cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("70000"), null), usuarioCajero.getUsername());
        });

        assertEquals("El usuario '" + usuarioCajero.getUsername() + "' ya tiene una sesión de caja abierta.", ex.getMessage());
    }

    @Test
    @DisplayName("Multi-caja: usuarios distintos pueden abrir cajas concurrentemente")
    void testAperturaConcurrenteDistintosUsuarios() {
        SesionCajaDTO s1 = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Caja 1"), usuarioCajero.getUsername());
        SesionCajaDTO s2 = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("80000"), "Caja 2"), otroCajero.getUsername());

        assertNotNull(s1.getId());
        assertNotNull(s2.getId());
        assertNotEquals(s1.getId(), s2.getId());

        List<SesionCajaDTO> abiertas = cajaService.obtenerSesionesAbiertas();
        assertEquals(2, abiertas.size());

        EstadoCajaDTO estadoCajero = cajaService.obtenerEstado(usuarioCajero.getUsername());
        assertTrue(estadoCajero.isAbierta());
        assertEquals(s1.getId(), estadoCajero.getSesionId());

        EstadoCajaDTO estadoOtro = cajaService.obtenerEstado(otroCajero.getUsername());
        assertTrue(estadoOtro.isAbierta());
        assertEquals(s2.getId(), estadoOtro.getSesionId());
    }

    @Test
    @DisplayName("Obtener estado reporta cerrada cuando no hay sesión activa")
    void testObtenerEstadoCajaCerrada() {
        EstadoCajaDTO estado = cajaService.obtenerEstado();

        assertFalse(estado.isAbierta());
        assertNull(estado.getSesionId());
        assertNull(estado.getCajeroApertura());
    }

    @Test
    @DisplayName("Arqueo y resumen en tiempo real calcula acumulados por método de pago excluyendo cancelados")
    void testResumenArqueoPorMetodoPago() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), null), usuarioCajero.getUsername());
        SesionCaja entidadSesion = sesionCajaRepository.findById(sesion.getId()).orElseThrow();

        // 1 pedido EFECTIVO (25.000)
        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("25000"))
                .build());

        // 1 pedido TARJETA_DEBITO (30.000)
        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.TARJETA_DEBITO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("30000"))
                .build());

        // 1 pedido TARJETA_CREDITO (45.000)
        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.TARJETA_CREDITO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("45000"))
                .build());

        // 1 pedido TRANSFERENCIA_QR (15.000)
        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.TRANSFERENCIA_QR)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("15000"))
                .build());

        // 1 pedido CANCELADO (20.000) - No debe computar
        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.CANCELADO)
                .total(new BigDecimal("20000"))
                .build());

        ResumenCajaDTO resumen = cajaService.obtenerResumenActual();

        assertEquals(sesion.getId(), resumen.getSesionId());
        assertEquals(0, new BigDecimal("50000").compareTo(resumen.getMontoInicial()));
        assertEquals(0, new BigDecimal("25000").compareTo(resumen.getTotalEfectivo()));
        assertEquals(0, new BigDecimal("30000").compareTo(resumen.getTotalTarjetaDebito()));
        assertEquals(0, new BigDecimal("45000").compareTo(resumen.getTotalTarjetaCredito()));
        assertEquals(0, new BigDecimal("15000").compareTo(resumen.getTotalTransferenciaQr()));
        assertEquals(0, new BigDecimal("115000").compareTo(resumen.getTotalVentas()));
        // totalEsperadoEfectivo = 50.000 inicial + 25.000 efectivo = 75.000
        assertEquals(0, new BigDecimal("75000").compareTo(resumen.getTotalEsperadoEfectivo()));
        assertEquals(4, resumen.getTotalPedidos());
    }

    @Test
    @DisplayName("Cierre de caja con sobrante de dinero calcula correctamente la diferencia")
    void testCerrarCajaConSobrante() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), null), usuarioCajero.getUsername());
        SesionCaja entidadSesion = sesionCajaRepository.findById(sesion.getId()).orElseThrow();

        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("25000"))
                .build());

        // Esperado en efectivo: 50.000 + 25.000 = 75.000
        // Cajero declara 80.000 (Sobrante de 5.000)
        CerrarCajaRequestDTO requestCierre = new CerrarCajaRequestDTO(new BigDecimal("80000"), "Cierre con propina/sobrante");

        SesionCajaDTO cerrada = cajaService.cerrarCaja(requestCierre, usuarioCajero.getUsername());

        assertEquals(EstadoSesionCaja.CERRADA, cerrada.getEstado());
        assertEquals(usuarioCajero.getUsername(), cerrada.getUsuarioCierre());
        assertNotNull(cerrada.getFechaCierre());
        assertEquals(0, new BigDecimal("75000").compareTo(cerrada.getMontoEsperadoEfectivo()));
        assertEquals(0, new BigDecimal("80000").compareTo(cerrada.getMontoRealEfectivo()));
        assertEquals(0, new BigDecimal("5000").compareTo(cerrada.getDiferencia()));
        assertEquals("Cierre con propina/sobrante", cerrada.getObservacionesCierre());

        // Tras cerrar, el estado debe indicar que está cerrada
        EstadoCajaDTO estadoActual = cajaService.obtenerEstado();
        assertFalse(estadoActual.isAbierta());
    }

    @Test
    @DisplayName("Cierre de caja con faltante de dinero calcula correctamente diferencia negativa")
    void testCerrarCajaConFaltante() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), null), usuarioCajero.getUsername());
        SesionCaja entidadSesion = sesionCajaRepository.findById(sesion.getId()).orElseThrow();

        pedidoRepository.save(Pedido.builder()
                .sesionCaja(entidadSesion)
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .total(new BigDecimal("25000"))
                .build());

        // Esperado: 75.000. Cajero declara 70.000 (Faltante de -5.000)
        CerrarCajaRequestDTO requestCierre = new CerrarCajaRequestDTO(new BigDecimal("70000"), "Faltante por vuelto");

        SesionCajaDTO cerrada = cajaService.cerrarCaja(requestCierre, usuarioCajero.getUsername());

        assertEquals(EstadoSesionCaja.CERRADA, cerrada.getEstado());
        assertEquals(0, new BigDecimal("75000").compareTo(cerrada.getMontoEsperadoEfectivo()));
        assertEquals(0, new BigDecimal("70000").compareTo(cerrada.getMontoRealEfectivo()));
        assertEquals(0, new BigDecimal("-5000").compareTo(cerrada.getDiferencia()));
    }

    @Test
    @DisplayName("Falla al cerrar caja si no hay una sesión abierta")
    void testCerrarCajaSinCajaAbiertaFalla() {
        CerrarCajaRequestDTO request = new CerrarCajaRequestDTO(new BigDecimal("50000"), null);

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> {
            cajaService.cerrarCaja(request, usuarioCajero.getUsername());
        });

        assertEquals("No existe una sesión de caja abierta para cerrar.", ex.getMessage());
    }

    @Test
    @DisplayName("Historial y consulta por ID devuelven sesiones registradas")
    void testHistorialYConsultaPorId() {
        SesionCajaDTO abierta = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("30000"), "Sesion 1"), usuarioCajero.getUsername());
        cajaService.cerrarCaja(new CerrarCajaRequestDTO(new BigDecimal("30000"), "Cierre 1"), usuarioCajero.getUsername());

        List<SesionCajaDTO> historial = cajaService.obtenerHistorial();
        assertFalse(historial.isEmpty());
        assertEquals(abierta.getId(), historial.get(0).getId());

        SesionCajaDTO porId = cajaService.obtenerPorId(abierta.getId());
        assertEquals(abierta.getId(), porId.getId());
        assertEquals(EstadoSesionCaja.CERRADA, porId.getEstado());
    }

    @Test
    @DisplayName("Cerrar caja por un cajero distinto al que la abrió arroja ReglaDeNegocioException")
    void testCerrarCajaPorOtroCajeroLanzaReglaDeNegocioException() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Turno Mañana"), usuarioCajero.getUsername());

        CerrarCajaRequestDTO requestCierre = new CerrarCajaRequestDTO(sesion.getId(), new BigDecimal("50000"), "Intento de cierre ajeno");

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> {
            cajaService.cerrarCaja(requestCierre, otroCajero.getUsername());
        });

        assertEquals("Solo el usuario que abrió la caja ('" + usuarioCajero.getUsername() + "') o un Administrador/Supervisor pueden realizar el arqueo y cierre.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Cerrar caja abierta por cajero puede ser realizada por un ADMIN")
    void testCerrarCajaPorAdminAutorizado() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Turno Cajero"), usuarioCajero.getUsername());

        CerrarCajaRequestDTO requestCierre = new CerrarCajaRequestDTO(sesion.getId(), new BigDecimal("50000"), "Cierre autorizado por Admin");
        SesionCajaDTO cerrada = cajaService.cerrarCaja(requestCierre, usuarioAdmin.getUsername());

        assertEquals(EstadoSesionCaja.CERRADA, cerrada.getEstado());
        assertEquals(usuarioAdmin.getUsername(), cerrada.getUsuarioCierre());
    }

    @Test
    @DisplayName("Cerrar caja abierta por cajero puede ser realizada por un OWNER")
    void testCerrarCajaPorOwnerAutorizado() {
        SesionCajaDTO sesion = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Turno Cajero"), usuarioCajero.getUsername());

        CerrarCajaRequestDTO requestCierre = new CerrarCajaRequestDTO(sesion.getId(), new BigDecimal("50000"), "Cierre autorizado por Owner");
        SesionCajaDTO cerrada = cajaService.cerrarCaja(requestCierre, usuarioOwner.getUsername());

        assertEquals(EstadoSesionCaja.CERRADA, cerrada.getEstado());
        assertEquals(usuarioOwner.getUsername(), cerrada.getUsuarioCierre());
    }

    @Test
    @DisplayName("Resumen por sesionId específico y por usuario")
    void testResumenPorSesionIdYUsuario() {
        SesionCajaDTO s1 = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("50000"), "Caja 1"), usuarioCajero.getUsername());
        SesionCajaDTO s2 = cajaService.abrirCaja(new AbrirCajaRequestDTO(new BigDecimal("90000"), "Caja 2"), otroCajero.getUsername());

        ResumenCajaDTO res1 = cajaService.obtenerResumen(s1.getId(), null);
        assertEquals(s1.getId(), res1.getSesionId());
        assertEquals(0, new BigDecimal("50000").compareTo(res1.getMontoInicial()));

        ResumenCajaDTO res2 = cajaService.obtenerResumen(null, otroCajero.getUsername());
        assertEquals(s2.getId(), res2.getSesionId());
        assertEquals(0, new BigDecimal("90000").compareTo(res2.getMontoInicial()));
    }
}
