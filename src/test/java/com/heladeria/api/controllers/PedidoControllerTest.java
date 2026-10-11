package com.heladeria.api.controllers;

import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.repositories.*;
import com.heladeria.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas de endpoints REST de Pedidos y consulta por trxId")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private Pedido pedidoGuardado;

    @BeforeEach
    void setUp() {
        pedidoRepository.deleteAll();

        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("ADMIN");
            return rolRepository.save(r);
        });

        if (usuarioRepository.findByUsername("admin").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRol(rolAdmin);
            admin.setEstado(true);
            admin.setPasswordNuncaExpira(true);
            admin.setDebeCambiarPassword(false);
            usuarioRepository.save(admin);
        }

        adminToken = jwtService.generateToken("admin", "ADMIN");

        Pedido pedido = Pedido.builder()
                .clienteNombre("Cliente Prueba Trx")
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.ENTREGADO)
                .trxId("TRX-TEST-20261011-999")
                .total(new BigDecimal("25000"))
                .fechaCreacion(LocalDateTime.now())
                .build();
        pedidoGuardado = pedidoRepository.save(pedido);
    }

    @Test
    @DisplayName("GET /api/pedidos/trx/{trxId} sin token devuelve 401 Unauthorized")
    void obtenerPorTrxIdSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/pedidos/trx/TRX-TEST-20261011-999"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/pedidos/trx/{trxId} con token válido devuelve 200 OK y el pedido completo")
    void obtenerPorTrxIdValidoDevuelve200() throws Exception {
        mockMvc.perform(get("/api/pedidos/trx/" + pedidoGuardado.getTrxId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pedidoGuardado.getId().intValue())))
                .andExpect(jsonPath("$.trxId", is(pedidoGuardado.getTrxId())))
                .andExpect(jsonPath("$.clienteNombre", is("Cliente Prueba Trx")))
                .andExpect(jsonPath("$.total", is(25000)));
    }

    @Test
    @DisplayName("GET /api/pedidos/trx/{trxId} con trxId inexistente devuelve 404 Not Found")
    void obtenerPorTrxIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/pedidos/trx/TRX-INEXISTENTE-000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/pedidos/trx/{trxId}/imprimir con trxId inexistente devuelve 404 Not Found")
    void imprimirPorTrxIdInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/api/pedidos/trx/TRX-INEXISTENTE-000/imprimir")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
