package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.SesionAuth;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.SesionAuthRepository;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Token de acceso corto + token de refresco rotativo + revocación real de sesiones. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Sesiones: refresco, rotación y revocación")
class SesionesAuthTest {

    private static final String CLAVE = "ClaveSegura#2026";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolRepository rolRepository;
    @Autowired private SesionAuthRepository sesionAuthRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private final ObjectMapper mapper = new ObjectMapper();

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        Usuario u = new Usuario();
        u.setUsername("u_sesiones");
        u.setPasswordHash(passwordEncoder.encode(CLAVE));
        u.setRol(rol);
        u.setEstado(true);
        u.setPasswordNuncaExpira(true);
        u.setDebeCambiarPassword(false);
        usuario = usuarioRepository.saveAndFlush(u);
    }

    private JsonNode login() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u_sesiones\",\"password\":\"" + CLAVE + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;

    private int llamarProtegido(String token) throws Exception {
        em.flush();
        em.clear(); // en producción cada petición tiene su propia transacción; aquí todo comparte una
        return mockMvc.perform(get("/api/caja/estado").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
    }

    private int refrescar(String refresh) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refresh + "\"}")).andReturn().getResponse().getStatus();
    }

    private JsonNode refrescarOk(String refresh) throws Exception {
        String body = mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    @DisplayName("El login entrega token de acceso y de refresco; el de acceso abre rutas protegidas")
    void loginEntregaAmbosTokens() throws Exception {
        JsonNode r = login();
        assertFalse(r.get("token").asText().isBlank());
        assertFalse(r.get("refreshToken").asText().isBlank());
        assertEquals(200, llamarProtegido(r.get("token").asText()));
    }

    @Test
    @DisplayName("Un token sin sesión (como los de 24 h anteriores a este cambio) ya no sirve")
    void tokenSinSesionSeRechaza() throws Exception {
        com.heladeria.api.security.JwtService jwt = new com.heladeria.api.security.JwtService(
                "q8F2xk0Lr9mZp3TvN7yB1cHs5WdE6gJaUoX4iKtRnMb2VfYe8ZlQwP0uSh3DjCaA", 900000, "jdbc:h2:mem:x");
        // firmado con otro secreto: tampoco debe pasar; y sin "sid" nunca pasa aunque la firma coincida
        assertEquals(401, llamarProtegido(jwt.generateToken("u_sesiones", "CAJERO")));
    }

    @Test
    @DisplayName("Refrescar rota el token: el nuevo sirve, el anterior reusado (fuera del margen) revoca la sesión")
    void rotacionYReuso() throws Exception {
        JsonNode r = login();
        String refresh1 = r.get("refreshToken").asText();
        JsonNode r2 = refrescarOk(refresh1);
        String refresh2 = r2.get("refreshToken").asText();
        assertNotEquals(refresh1, refresh2);
        assertEquals(200, llamarProtegido(r2.get("token").asText()));

        // reuso inmediato: dentro del margen se rechaza sin matar la sesión (carrera entre pestañas)
        assertEquals(401, refrescar(refresh1));
        assertEquals(200, refrescar(refresh2));

        // reuso tardío: se interpreta como token copiado y se revoca toda la sesión
        SesionAuth s = sesionAuthRepository.findAll().get(0);
        s.setRotadoEn(LocalDateTime.now().minusMinutes(5));
        sesionAuthRepository.saveAndFlush(s);
        assertNotNull(s.getRefreshHashAnterior());
        // refresh2 pasó a ser el token "anterior" tras el último refresco y ya está fuera del margen
        assertEquals(401, refrescar(refresh2));
        assertEquals(401, llamarProtegido(r2.get("token").asText()));
    }

    @Test
    @DisplayName("Logout revoca la sesión: ni el token de acceso ni el de refresco vuelven a servir")
    void logoutRevoca() throws Exception {
        JsonNode r = login();
        assertEquals(200, llamarProtegido(r.get("token").asText()));

        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + r.get("refreshToken").asText() + "\"}")).andExpect(status().isNoContent());

        assertEquals(401, llamarProtegido(r.get("token").asText()));
        assertEquals(401, refrescar(r.get("refreshToken").asText()));
    }

    @Test
    @DisplayName("Cambiar la contraseña cierra las demás sesiones del usuario")
    void cambioDeClaveRevocaSesiones() throws Exception {
        JsonNode vieja = login();
        mockMvc.perform(post("/api/auth/cambiar-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"u_sesiones\",\"passwordActual\":\"" + CLAVE + "\",\"passwordNueva\":\"OtraClave#2027x\"}"))
                .andExpect(status().isOk());

        assertEquals(401, llamarProtegido(vieja.get("token").asText()));
        assertEquals(401, refrescar(vieja.get("refreshToken").asText()));
    }

    @Test
    @DisplayName("Un usuario desactivado no puede refrescar")
    void usuarioDesactivadoNoRefresca() throws Exception {
        JsonNode r = login();
        usuario.setEstado(false);
        usuarioRepository.saveAndFlush(usuario);
        assertEquals(401, refrescar(r.get("refreshToken").asText()));
    }

    @Test
    @DisplayName("Una sesión expirada no se puede refrescar")
    void sesionExpirada() throws Exception {
        JsonNode r = login();
        SesionAuth s = sesionAuthRepository.findAll().get(0);
        s.setExpiraEn(LocalDateTime.now().minusMinutes(1));
        sesionAuthRepository.saveAndFlush(s);
        assertEquals(401, refrescar(r.get("refreshToken").asText()));
    }
}
