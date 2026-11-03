package com.heladeria.api.controllers;

import com.heladeria.api.TestTokens;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.services.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/** Lo que el sistema NO debe exponer: consola H2, Swagger, datos fiscales sin sesión y privilegios por permiso de página. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Exposición pública y escalada de privilegios")
class ExposicionPublicaTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TestTokens testTokens;
    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("La consola H2 y Swagger no están disponibles por defecto")
    void consolaYSwaggerApagados() throws Exception {
        for (String ruta : List.of("/h2-console/", "/swagger-ui/index.html", "/swagger-ui.html", "/v3/api-docs", "/api-docs")) {
            int estado = mockMvc.perform(get(ruta)).andReturn().getResponse().getStatus();
            assertTrue(estado == 401 || estado == 403 || estado == 404, ruta + " respondió " + estado);
        }
    }

    @Test
    @DisplayName("Una ruta cualquiera fuera de /api ya no queda abierta")
    void rutasDesconocidasCerradas() throws Exception {
        int estado = mockMvc.perform(get("/cualquier-cosa")).andReturn().getResponse().getStatus();
        assertTrue(estado == 401 || estado == 403 || estado == 404, "respondió " + estado);
    }

    @Test
    @DisplayName("Sin sesión /api/institucion solo da el nombre y el logo; con sesión, los datos fiscales")
    void institucionRecortadaSinSesion() throws Exception {
        mockMvc.perform(get("/api/institucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").exists())
                .andExpect(jsonPath("$.ruc").doesNotExist())
                .andExpect(jsonPath("$.timbrado").doesNotExist())
                .andExpect(jsonPath("$.direccion").doesNotExist());

        String token = testTokens.emitir("admin", "ADMIN");
        mockMvc.perform(get("/api/institucion").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruc").exists());
    }

    @Test
    @DisplayName("Con solo el permiso de página 'backup' no se puede descargar ni restaurar la base")
    void backupSensibleSoloAdministradores() throws Exception {
        var soloPagina = user("encargado").authorities(new SimpleGrantedAuthority("PAGINA_backup"));
        mockMvc.perform(get("/api/backup/descargar/cualquiera.dump").with(soloPagina)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/backup/restaurar-servidor/cualquiera.dump").with(soloPagina)).andExpect(status().isForbidden());
        // listar sí está permitido con el permiso de página
        mockMvc.perform(get("/api/backup/listar").with(soloPagina)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Quien gestiona usuarios por permiso de página no puede tocar a un administrador")
    void permisoDePaginaNoEscalaPrivilegios() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "encargado", null, List.of(new SimpleGrantedAuthority("PAGINA_usuarios"))));
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();

        com.heladeria.api.dto.UsuarioDTO cambio = new com.heladeria.api.dto.UsuarioDTO();
        cambio.setUsername("admin");
        cambio.setPassword("ClaveRobada#2026");
        ReglaDeNegocioException e = assertThrows(ReglaDeNegocioException.class, () -> usuarioService.actualizar(admin.getId(), cambio));
        assertTrue(e.getMessage().contains("Administrador o Propietario"), e.getMessage());
        assertThrows(ReglaDeNegocioException.class, () -> usuarioService.eliminar(admin.getId()));
    }
}
