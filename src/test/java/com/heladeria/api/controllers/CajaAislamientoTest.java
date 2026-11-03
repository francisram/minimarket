package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.AbrirCajaRequestDTO;
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

/** Un cajero no puede ver, operar ni cerrar la caja de otro compañero. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Aislamiento entre cajas (autorización cruzada)")
class CajaAislamientoTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private SesionCajaRepository sesionCajaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String tokenA;
    private String tokenB;
    private String tokenAdmin;
    private long sesionIdA;

    private void crearUsuario(String username, String rolNombre, boolean activo) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }
        Rol rol = rolRepository.findByNombreRol(rolNombre).orElseThrow();
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode("x12345"));
        u.setRol(rol);
        u.setEstado(activo);
        u.setPasswordNuncaExpira(true);
        u.setDebeCambiarPassword(false);
        usuarioRepository.save(u);
    }

    @BeforeEach
    void setUp() throws Exception {
        sesionCajaRepository.deleteAll();
        crearUsuario("cajero_a_aisl", "CAJERO", true);
        crearUsuario("cajero_b_aisl", "CAJERO", true);
        tokenA = jwtService.generateToken("cajero_a_aisl", "CAJERO");
        tokenB = jwtService.generateToken("cajero_b_aisl", "CAJERO");
        tokenAdmin = jwtService.generateToken("admin", "ADMIN");

        String json = mockMvc.perform(post("/api/caja/abrir")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AbrirCajaRequestDTO(new BigDecimal("100000"), "A"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        sesionIdA = objectMapper.readTree(json).get("id").asLong();
    }

    @Test
    @DisplayName("B no puede ver el detalle, los movimientos ni el resumen de la sesión de A (403)")
    void lecturaCruzadaProhibida() throws Exception {
        mockMvc.perform(get("/api/caja/" + sesionIdA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/caja/" + sesionIdA + "/movimientos").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/caja/resumen").param("sesionId", String.valueOf(sesionIdA))
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("B no puede registrar movimientos en la sesión de A (403)")
    void movimientoCruzadoProhibido() throws Exception {
        String body = "{\"sesionId\":" + sesionIdA + ",\"tipo\":\"EGRESO\",\"categoria\":\"GASTO_MENOR\",\"concepto\":\"robo\",\"monto\":5000}";
        mockMvc.perform(post("/api/caja/movimientos").header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("B no puede cerrar la sesión de A, y la sesión sigue abierta")
    void cierreCruzadoProhibido() throws Exception {
        String body = "{\"sesionId\":" + sesionIdA + ",\"montoRealEfectivo\":100000,\"observaciones\":\"forzado\"}";
        mockMvc.perform(post("/api/caja/cerrar").header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/caja/" + sesionIdA).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    @DisplayName("El titular y un ADMIN sí pueden ver la sesión")
    void titularYAdminAccedenOk() throws Exception {
        mockMvc.perform(get("/api/caja/" + sesionIdA).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/caja/" + sesionIdA + "/movimientos").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("El historial de un cajero solo incluye sus propias sesiones; el ADMIN ve todas")
    void historialFiltradoPorUsuario() throws Exception {
        mockMvc.perform(get("/api/caja/historial").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/caja/historial").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id==" + sesionIdA + ")]", hasSize(1)));
    }

    @Test
    @DisplayName("Un usuario desactivado pierde el acceso aunque su token no haya expirado (401)")
    void usuarioDesactivadoPierdeAcceso() throws Exception {
        Usuario b = usuarioRepository.findByUsername("cajero_b_aisl").orElseThrow();
        b.setEstado(false);
        usuarioRepository.saveAndFlush(b);
        mockMvc.perform(get("/api/caja/estado").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isUnauthorized());
    }
}
