package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.InstitucionDTO;
import com.heladeria.api.entities.Pagina;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.PaginaRepository;
import com.heladeria.api.repositories.RolRepository;
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

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas de seguridad y endpoints de Institución")
class InstitucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PaginaRepository paginaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String cajeroToken;
    private String operadorInstitucionToken;

    @BeforeEach
    void setUp() {
        // Rol ADMIN
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

        // Rol CAJERO (sin pagina institucion)
        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });

        if (usuarioRepository.findByUsername("cajero").isEmpty()) {
            Usuario cajero = new Usuario();
            cajero.setUsername("cajero");
            cajero.setPasswordHash(passwordEncoder.encode("cajero123"));
            cajero.setRol(rolCajero);
            cajero.setEstado(true);
            cajero.setPasswordNuncaExpira(true);
            cajero.setDebeCambiarPassword(false);
            usuarioRepository.save(cajero);
        }

        // Pagina institucion
        Pagina paginaInstitucion = paginaRepository.findByClave("institucion").orElseGet(() -> {
            Pagina p = new Pagina(null, "institucion", "Datos del Negocio", "/institucion", null, "building", 78);
            return paginaRepository.save(p);
        });

        // Rol personalizado OPERADOR_CONFIG con permiso PAGINA_institucion
        Rol rolConfig = rolRepository.findByNombreRol("OPERADOR_CONFIG").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("OPERADOR_CONFIG");
            r.setPaginas(new java.util.HashSet<>(java.util.Set.of(paginaInstitucion)));
            return rolRepository.save(r);
        });
        rolConfig.setPaginas(new java.util.HashSet<>(java.util.Set.of(paginaInstitucion)));
        rolRepository.save(rolConfig);

        if (usuarioRepository.findByUsername("operador_inst").isEmpty()) {
            Usuario operador = new Usuario();
            operador.setUsername("operador_inst");
            operador.setPasswordHash(passwordEncoder.encode("operador123"));
            operador.setRol(rolConfig);
            operador.setEstado(true);
            operador.setPasswordNuncaExpira(true);
            operador.setDebeCambiarPassword(false);
            usuarioRepository.save(operador);
        }

        adminToken = jwtService.generateToken("admin", "ADMIN");
        cajeroToken = jwtService.generateToken("cajero", "CAJERO");
        operadorInstitucionToken = jwtService.generateToken("operador_inst", "OPERADOR_CONFIG");
    }

    @Test
    @DisplayName("GET /institucion es público y no requiere autenticación")
    void getInstitucionSinTokenRetorna200() throws Exception {
        mockMvc.perform(get("/institucion"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /institucion sin autenticación devuelve 401 Unauthorized")
    void putInstitucionSinTokenRetorna401() throws Exception {
        InstitucionDTO dto = new InstitucionDTO();
        dto.setNombre("Nuevo Nombre");

        mockMvc.perform(put("/institucion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /institucion con usuario sin permiso (ej. CAJERO) devuelve 403 Forbidden")
    void putInstitucionConRolSinPermisoRetorna403() throws Exception {
        InstitucionDTO dto = new InstitucionDTO();
        dto.setNombre("Nuevo Nombre");

        mockMvc.perform(put("/institucion")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /institucion con autoridad PAGINA_institucion permite actualizar exitosamente")
    void putInstitucionConAutoridadPaginaInstitucionRetorna200() throws Exception {
        InstitucionDTO dto = new InstitucionDTO();
        dto.setNombre("Heladería Modificada por Operador");
        dto.setRuc("1234567-8");

        mockMvc.perform(put("/institucion")
                        .header("Authorization", "Bearer " + operadorInstitucionToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Heladería Modificada por Operador"));
    }

    @Test
    @DisplayName("PUT /institucion con rol ADMIN permite actualizar exitosamente")
    void putInstitucionConAdminRetorna200() throws Exception {
        InstitucionDTO dto = new InstitucionDTO();
        dto.setNombre("Heladería Modificada por Admin");
        dto.setRuc("9876543-2");

        mockMvc.perform(put("/institucion")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Heladería Modificada por Admin"));
    }
}
