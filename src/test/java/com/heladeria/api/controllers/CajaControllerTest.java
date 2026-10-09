package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.AbrirCajaRequestDTO;
import com.heladeria.api.dto.CerrarCajaRequestDTO;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.SesionCajaRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas del controlador y endpoints REST de Caja")
class CajaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SesionCajaRepository sesionCajaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String cajeroToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        sesionCajaRepository.deleteAll();

        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        if (usuarioRepository.findByUsername("cajero_caja_ctrl").isEmpty()) {
            Usuario cajero = new Usuario();
            cajero.setUsername("cajero_caja_ctrl");
            cajero.setPasswordHash(passwordEncoder.encode("cajero123"));
            cajero.setRol(rolCajero);
            cajero.setEstado(true);
            cajero.setPasswordNuncaExpira(true);
            cajero.setDebeCambiarPassword(false);
            usuarioRepository.save(cajero);
        }

        cajeroToken = jwtService.generateToken("cajero_caja_ctrl", "CAJERO");
        adminToken = jwtService.generateToken("admin", "ADMIN");
    }

    @Test
    @DisplayName("Petición sin autenticación a /api/caja/estado devuelve 401 Unauthorized")
    void estadoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/caja/estado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Consultar estado con caja cerrada devuelve abierta: false")
    void estadoConCajaCerradaDevuelveFalse() throws Exception {
        mockMvc.perform(get("/api/caja/estado")
                        .header("Authorization", "Bearer " + cajeroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.abierta").value(false))
                .andExpect(jsonPath("$.sesionId").doesNotExist());
    }

    @Test
    @DisplayName("Abrir caja exitosamente con token de CAJERO devuelve 201 Created y estado ABIERTA")
    void abrirCajaExitoso() throws Exception {
        AbrirCajaRequestDTO req = new AbrirCajaRequestDTO(new BigDecimal("100000"), "Apertura turno");

        mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.usuarioApertura").value("cajero_caja_ctrl"))
                .andExpect(jsonPath("$.montoInicial").value(100000));
    }

    @Test
    @DisplayName("Abrir caja con monto negativo falla validación Bean Validation (400 Bad Request)")
    void abrirCajaMontoNegativoDevuelve400() throws Exception {
        AbrirCajaRequestDTO req = new AbrirCajaRequestDTO(new BigDecimal("-5000"), "Monto invalido");

        mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.montoInicial").exists());
    }

    @Test
    @DisplayName("Intentar abrir caja cuando ya hay una abierta devuelve 400 Bad Request por regla de negocio")
    void abrirCajaDuplicadaDevuelve400() throws Exception {
        AbrirCajaRequestDTO req = new AbrirCajaRequestDTO(new BigDecimal("50000"), "Primera apertura");

        mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una caja abierta en el sistema."));
    }

    @Test
    @DisplayName("Cerrar caja exitosamente devuelve 200 OK y estado CERRADA")
    void cerrarCajaExitoso() throws Exception {
        AbrirCajaRequestDTO reqApertura = new AbrirCajaRequestDTO(new BigDecimal("50000"), null);
        mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqApertura)))
                .andExpect(status().isCreated());

        CerrarCajaRequestDTO reqCierre = new CerrarCajaRequestDTO(new BigDecimal("50000"), "Cierre exacto");

        mockMvc.perform(post("/api/caja/cerrar")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqCierre)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"))
                .andExpect(jsonPath("$.usuarioCierre").value("cajero_caja_ctrl"))
                .andExpect(jsonPath("$.montoRealEfectivo").value(50000))
                .andExpect(jsonPath("$.diferencia").value(0));
    }

    @Test
    @DisplayName("Historial y consulta por ID devuelven sesiones registradas")
    void historialYDetallePorId() throws Exception {
        AbrirCajaRequestDTO reqApertura = new AbrirCajaRequestDTO(new BigDecimal("20000"), null);
        String aperturaResp = mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqApertura)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long sesionId = objectMapper.readTree(aperturaResp).get("id").asLong();

        mockMvc.perform(get("/api/caja/historial")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").value(sesionId));

        mockMvc.perform(get("/api/caja/" + sesionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sesionId))
                .andExpect(jsonPath("$.montoInicial").value(20000));
    }

    @Test
    @DisplayName("Consultar ID inexistente devuelve 404 Not Found")
    void sesionInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/caja/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
