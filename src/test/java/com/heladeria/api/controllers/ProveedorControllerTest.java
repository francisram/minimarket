package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.ProveedorRequestDTO;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.ProveedorRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas de validación RUC en ProveedorController")
class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.heladeria.api.TestTokens testTokens;

    private String adminToken;

    @BeforeEach
    void setUp() {
        Rol rolAdmin = rolRepository.findByNombreRol("ADMIN").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("ADMIN");
            return rolRepository.save(r);
        });

        Usuario admin = usuarioRepository.findByUsername("admin_prov_test").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("admin_prov_test");
            u.setPasswordHash(passwordEncoder.encode("123456"));
            u.setRol(rolAdmin);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        adminToken = testTokens.emitir("admin_prov_test", "ADMIN");
    }

    @Test
    @DisplayName("Crear proveedor con RUC formal válido (80106020-6) responde 201 Created")
    void testCrearProveedorRucValido() throws Exception {
        ProveedorRequestDTO req = new ProveedorRequestDTO("80106020-6", "Lácteos del Valle S.A.", "Juan Benítez", "Km 14", "021-123456", "valle@test.com", true);

        mockMvc.perform(post("/api/proveedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ruc").value("80106020-6"))
                .andExpect(jsonPath("$.razonSocial").value("Lácteos del Valle S.A."));
    }

    @Test
    @DisplayName("Crear proveedor con DV erróneo (80106020-0) responde 400 Bad Request")
    void testCrearProveedorDvErroneo() throws Exception {
        ProveedorRequestDTO req = new ProveedorRequestDTO("80106020-0", "Proveedor Inválido", "Contacto", "Dirección", "021-000", "bad@test.com", true);

        mockMvc.perform(post("/api/proveedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación de campos"))
                .andExpect(jsonPath("$.campos.ruc", containsString("Módulo 11")));
    }

    @Test
    @DisplayName("Crear proveedor con número sin guión responde 400 Bad Request (proveedores exigen BASE-DV)")
    void testCrearProveedorSinDv() throws Exception {
        ProveedorRequestDTO req = new ProveedorRequestDTO("80106020", "Proveedor Sin DV", "Contacto", "Dirección", "021-000", "sindv@test.com", true);

        mockMvc.perform(post("/api/proveedores")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de validación de campos"))
                .andExpect(jsonPath("$.campos.ruc", containsString("Módulo 11")));
    }
}
