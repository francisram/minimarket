package com.heladeria.api.controllers;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Flujo de cambio de contraseña obligatorio (usuarios sembrados con una clave temporal). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Cambio de contraseña obligatorio")
class CambioPasswordObligatorioTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        Usuario u = new Usuario();
        u.setUsername("u_cambio_obligatorio");
        u.setPasswordHash(passwordEncoder.encode("admin123"));
        u.setRol(rol);
        u.setEstado(true);
        u.setPasswordNuncaExpira(true);
        u.setDebeCambiarPassword(true);
        usuarioRepository.saveAndFlush(u);
    }

    private String login(String clave) {
        return "{\"username\":\"u_cambio_obligatorio\",\"password\":\"" + clave + "\"}";
    }

    private String cambio(String actual, String nueva) {
        return "{\"username\":\"u_cambio_obligatorio\",\"passwordActual\":\"" + actual + "\",\"passwordNueva\":\"" + nueva + "\"}";
    }

    @Test
    @DisplayName("El login con la clave temporal exige cambiarla; el cambio la acepta, devuelve sesión y la clave vieja deja de servir")
    void flujoCompleto() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("admin123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.motivo").value("DEBE_CAMBIAR_PASSWORD"));

        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content(cambio("admin123", "MiClaveNueva#2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.username").value("u_cambio_obligatorio"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("MiClaveNueva#2026")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(login("admin123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("No se acepta la misma clave, una clave de demostración ni una demasiado corta")
    void rechazaClavesInaceptables() throws Exception {
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content(cambio("admin123", "admin123")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content(cambio("admin123", "owner123")))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content(cambio("admin123", "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.passwordNueva").exists());
    }

    @Test
    @DisplayName("La petición del frontend anterior (sin username y con nuevaPassword) falla la validación")
    void formatoViejoFallaValidacion() throws Exception {
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"admin123\",\"nuevaPassword\":\"MiClaveNueva#2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.username").exists());
    }
}
