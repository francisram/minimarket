package com.heladeria.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.LoginRequestDTO;
import com.heladeria.api.dto.RolDTO;
import com.heladeria.api.entities.Licencia;
import com.heladeria.api.entities.PlanLicencia;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.LicenciaRepository;
import com.heladeria.api.repositories.PlanLicenciaRepository;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAndLicensingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PlanLicenciaRepository planLicenciaRepository;

    @Autowired
    private LicenciaRepository licenciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenAdmin;
    private String tokenOwner;
    private String tokenCajero;

    @BeforeEach
    void setUp() {
        // Asegurar usuario cajero para pruebas
        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        if (usuarioRepository.findByUsername("cajero_test").isEmpty()) {
            Usuario cajero = new Usuario();
            cajero.setUsername("cajero_test");
            cajero.setPasswordHash(passwordEncoder.encode("cajero123"));
            cajero.setRol(rolCajero);
            cajero.setEstado(true);
            cajero.setPasswordNuncaExpira(true);
            cajero.setDebeCambiarPassword(false);
            usuarioRepository.save(cajero);
        }

        tokenAdmin = jwtService.generateToken("admin", "ADMIN");
        tokenOwner = jwtService.generateToken("owner", "OWNER");
        tokenCajero = jwtService.generateToken("cajero_test", "CAJERO");
    }

    @Test
    @DisplayName("Login exitoso devuelve token JWT y datos de usuario")
    void testLoginExitoso() throws Exception {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername("admin");
        req.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("Login con credenciales incorrectas devuelve 401")
    void testLoginPasswordInvalida() throws Exception {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername("admin");
        req.setPassword("clave_incorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Petición sin token a endpoint protegido devuelve 401 Unauthorized")
    void testAccesoProtegidoSinToken() throws Exception {
        mockMvc.perform(get("/api/sabores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario con permiso de página accede correctamente")
    void testAccesoConTokenYPermisoValido() throws Exception {
        mockMvc.perform(get("/api/sabores")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuario cajero sin permiso de página 'usuarios' recibe 403 Forbidden")
    void testCajeroSinPermisoPaginaUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenCajero))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario OWNER tiene bypass y puede acceder a gestión de usuarios")
    void testOwnerBypassAcceso() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Listar páginas disponibles devuelve sólo las autorizadas en la licencia activa")
    void testListarPaginasDisponiblesSegunLicencia() throws Exception {
        mockMvc.perform(get("/api/paginas/disponibles")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].clave", hasItem("sabores")));
    }

    @Test
    @DisplayName("Intento de crear un rol con páginas fuera de la licencia activa genera 409 Conflict")
    void testConflictoLicenciaAlCrearRol() throws Exception {
        // Reducimos temporalmente la licencia activa al plan Basico (no incluye 'usuarios')
        PlanLicencia basico = planLicenciaRepository.findByNombre("Basico").orElseThrow();
        Licencia licencia = licenciaRepository.findById(1L).orElseThrow();
        licencia.setPlan(basico);
        licenciaRepository.save(licencia);

        // Intentamos crear un nuevo rol que solicite la página 'usuarios' (que no está en Basico)
        RolDTO rolDTO = new RolDTO();
        rolDTO.setNombre("ROL_CON_CONFLICTO");
        rolDTO.setPaginas(java.util.Set.of("sabores", "usuarios"));

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de estado o licenciamiento"));
    }

    @Test
    @DisplayName("Endpoint /api/stock/alertas accesible para ADMIN y devuelve estructura correcta")
    void testVerificarAccesoEndpointStockAlertas() throws Exception {
        mockMvc.perform(get("/api/stock/alertas")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productosBajoStock").isArray())
                .andExpect(jsonPath("$.saboresBajoStock").isArray())
                .andExpect(jsonPath("$.presentacionesBajoStock").isArray())
                .andExpect(jsonPath("$.totalAlertas").isNumber());
    }

    @Test
    @DisplayName("Crear rol asignando páginas 'stock' e 'impresoras' en plan Premium es exitoso")
    void testCrearRolConStockEImpresorasExitoso() throws Exception {
        RolDTO nuevoRol = new RolDTO();
        nuevoRol.setNombre("SUPERVISOR_STOCK");
        nuevoRol.setPaginas(java.util.Set.of("stock", "impresoras", "dashboard"));

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevoRol)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("SUPERVISOR_STOCK"))
                .andExpect(jsonPath("$.paginas", hasItem("stock")))
                .andExpect(jsonPath("$.paginas", hasItem("impresoras")));
    }
}
