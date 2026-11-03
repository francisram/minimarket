package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.TestTokens;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** "Olvidé mi contraseña" sin correo: un administrador la restablece y el usuario debe cambiarla al ingresar. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Restablecer contraseña")
class RestablecerPasswordTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TestTokens testTokens;
    private final ObjectMapper mapper = new ObjectMapper();

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;

    private Usuario cajero;
    private Usuario admin2;
    private String tokenAdmin;
    private String tokenOwner;
    private String tokenCajero;

    private Usuario crear(String username, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode("ClaveVieja#2026"));
        u.setRol(rolRepository.findByNombreRol(rol).orElseThrow());
        u.setEstado(true);
        u.setPasswordNuncaExpira(true);
        u.setDebeCambiarPassword(false);
        return usuarioRepository.saveAndFlush(u);
    }

    @BeforeEach
    void setUp() {
        cajero = crear("cajero_olvido", "CAJERO");
        admin2 = crear("admin_dos_olvido", "ADMIN");
        crear("admin_caller", "ADMIN");
        crear("owner_caller", "OWNER");
        tokenAdmin = testTokens.emitir("admin_caller", "ADMIN");
        tokenOwner = testTokens.emitir("owner_caller", "OWNER");
        tokenCajero = testTokens.emitir("cajero_olvido", "CAJERO");
    }

    private String restablecer(Long id, String token, int estadoEsperado) throws Exception {
        return mockMvc.perform(post("/api/usuarios/" + id + "/restablecer-password").header("Authorization", "Bearer " + token))
                .andExpect(status().is(estadoEsperado)).andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("El administrador obtiene una clave temporal; el usuario debe cambiarla y sus sesiones se cierran")
    void restablecerFuncionaYObligaCambio() throws Exception {
        String temporal = mapper.readTree(restablecer(cajero.getId(), tokenAdmin, 200)).get("passwordTemporal").asText();
        assertEquals(12, temporal.length());

        // las sesiones abiertas del usuario se cerraron (en producción cada petición tiene su propia transacción)
        em.flush();
        em.clear();
        assertEquals(401, mockMvc.perform(get("/api/caja/estado").header("Authorization", "Bearer " + tokenCajero))
                .andReturn().getResponse().getStatus());
        // la clave vieja ya no sirve
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"cajero_olvido\",\"password\":\"ClaveVieja#2026\"}")).andExpect(status().isUnauthorized());
        // con la temporal se exige cambiar la contraseña
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"cajero_olvido\",\"password\":\"" + temporal + "\"}")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"cajero_olvido\",\"passwordActual\":\"" + temporal + "\",\"passwordNueva\":\"NuevaPropia#2027\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Un cajero no puede restablecer contraseñas (403)")
    void cajeroNoPuede() throws Exception {
        restablecer(admin2.getId(), tokenCajero, 403);
    }

    @Test
    @DisplayName("Un administrador no puede restablecer la de otro administrador ni la propia; el OWNER sí la de un administrador")
    void jerarquia() throws Exception {
        restablecer(admin2.getId(), tokenAdmin, 400);
        Long idCaller = usuarioRepository.findByUsername("admin_caller").orElseThrow().getId();
        restablecer(idCaller, tokenAdmin, 400);
        restablecer(admin2.getId(), tokenOwner, 200);
    }

    @Test
    @DisplayName("Las contraseñas temporales son distintas cada vez")
    void aleatorias() {
        assertNotEquals(com.heladeria.api.services.UsuarioService.generarPasswordTemporal(), com.heladeria.api.services.UsuarioService.generarPasswordTemporal());
    }
}
