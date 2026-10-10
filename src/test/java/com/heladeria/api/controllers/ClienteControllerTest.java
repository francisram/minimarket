package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.dto.ClienteRequestDTO;
import com.heladeria.api.entities.Cliente;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.ClienteRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas del controlador y endpoints REST de Clientes")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String cajeroToken;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        clienteRepository.flush();

        Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseGet(() -> {
            Rol r = new Rol();
            r.setNombreRol("CAJERO");
            return rolRepository.save(r);
        });

        Usuario cajero = usuarioRepository.findByUsername("cajero_test").orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername("cajero_test");
            u.setPasswordHash(passwordEncoder.encode("123456"));
            u.setRol(rolCajero);
            u.setEstado(true);
            return usuarioRepository.save(u);
        });

        cajeroToken = jwtService.generateToken("cajero_test", "CAJERO");
    }

    @Test
    @DisplayName("Endpoint /api/clientes sin autenticación responde 401 Unauthorized")
    void testListarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Listar clientes con token de cajero responde 200 OK")
    void testListarConTokenCajero() throws Exception {
        Cliente c = new Cliente();
        c.setRuc("1234567-8");
        c.setRazonSocial("Juan Pérez");
        clienteRepository.saveAndFlush(c);

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer " + cajeroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ruc").value("1234567-8"))
                .andExpect(jsonPath("$[0].razonSocial").value("Juan Pérez"));
    }

    @Test
    @DisplayName("Buscar cliente por RUC responde 200 si existe y 404 si no existe")
    void testBuscarPorRuc() throws Exception {
        Cliente c = new Cliente();
        c.setRuc("88812345-6");
        c.setRazonSocial("Distribuidora Central");
        clienteRepository.saveAndFlush(c);

        mockMvc.perform(get("/api/clientes/buscar")
                        .param("ruc", "88812345-6")
                        .header("Authorization", "Bearer " + cajeroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruc").value("88812345-6"))
                .andExpect(jsonPath("$.razonSocial").value("Distribuidora Central"));

        mockMvc.perform(get("/api/clientes/buscar")
                        .param("ruc", "0000000-0")
                        .header("Authorization", "Bearer " + cajeroToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Crear nuevo cliente responde 201 Created")
    void testCrearCliente() throws Exception {
        ClienteRequestDTO req = new ClienteRequestDTO("9876543-2", "Comercial San Roque", "Av. Eusebio Ayala", "021-999888", "info@sanroque.com", true);

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ruc").value("9876543-2"))
                .andExpect(jsonPath("$.razonSocial").value("Comercial San Roque"));
    }

    @Test
    @DisplayName("Cambiar estado de cliente responde 200 OK con nuevo estado")
    void testCambiarEstado() throws Exception {
        Cliente c = new Cliente();
        c.setRuc("5555555-5");
        c.setRazonSocial("Cliente Test");
        c.setActivo(true);
        c = clienteRepository.save(c);

        mockMvc.perform(patch("/api/clientes/" + c.getId() + "/estado")
                        .param("activo", "false")
                        .header("Authorization", "Bearer " + cajeroToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
