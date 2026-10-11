package com.heladeria.api.controllers;

import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas de endpoints REST del controlador de Reportes")
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String adminToken;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("Petición sin autenticación a /api/reportes/dashboard devuelve 401 Unauthorized")
    void dashboardSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/reportes/dashboard con token de ADMIN devuelve 200 OK y estructura de métricas")
    void dashboardConTokenDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").exists())
                .andExpect(jsonPath("$.cantidadVentas").isNumber())
                .andExpect(jsonPath("$.ticketPromedio").exists())
                .andExpect(jsonPath("$.totalEfectivo").exists())
                .andExpect(jsonPath("$.ventasPorDia").isArray())
                .andExpect(jsonPath("$.topProductos").isArray())
                .andExpect(jsonPath("$.topSabores").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/ventas con token devuelve 200 OK y listado")
    void reporteVentasDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/ventas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").exists())
                .andExpect(jsonPath("$.pedidos").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/ventas con trxId devuelve 200 OK y estructura filtrada")
    void reporteVentasConTrxIdDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/ventas?trxId=TRX-123")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/libro-iva devuelve 200 OK con liquidación tributaria")
    void libroIvaDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/libro-iva")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granTotal").exists())
                .andExpect(jsonPath("$.totalIva").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/libro-iva/csv descarga archivo CSV con headers correctos")
    void descargarLibroIvaCsvDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/libro-iva/csv")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment; filename=\"libro_ventas_iva.csv\"")))
                .andExpect(content().contentType("text/csv;charset=UTF-8"));
    }

    @Test
    @DisplayName("GET /api/reportes/cajas devuelve 200 OK con auditoría de turnos")
    void reporteCajasDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/cajas")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSesiones").isNumber())
                .andExpect(jsonPath("$.totalSobrante").exists())
                .andExpect(jsonPath("$.totalFaltante").exists())
                .andExpect(jsonPath("$.sesiones").isArray());
    }

    @Test
    @DisplayName("GET /api/reportes/ranking-productos devuelve 200 OK con rankings")
    void rankingProductosDevuelve200() throws Exception {
        mockMvc.perform(get("/api/reportes/ranking-productos?limite=5")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topPresentaciones").isArray())
                .andExpect(jsonPath("$.topProductosSimples").isArray())
                .andExpect(jsonPath("$.topSabores").isArray())
                .andExpect(jsonPath("$.topToppings").isArray());
    }
}
