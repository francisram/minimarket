package com.heladeria.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.Institucion;
import com.heladeria.api.entities.Ticketera;
import com.heladeria.api.entities.TipoConexion;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.TicketeraRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Pruebas del controlador y endpoints de Impresora")
class ImpresoraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImpresoraRepository impresoraRepository;

    @Autowired
    private TicketeraRepository ticketeraRepository;

    @Autowired
    private InstitucionRepository institucionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String cajeroToken;

    @BeforeEach
    void setUp() {
        ticketeraRepository.deleteAll();
        impresoraRepository.deleteAll();

        com.heladeria.api.entities.Rol rolCajero = rolRepository.findByNombreRol("CAJERO").orElseThrow();
        if (usuarioRepository.findByUsername("cajero").isEmpty()) {
            com.heladeria.api.entities.Usuario cajero = new com.heladeria.api.entities.Usuario();
            cajero.setUsername("cajero");
            cajero.setPasswordHash(passwordEncoder.encode("cajero123"));
            cajero.setRol(rolCajero);
            cajero.setEstado(true);
            cajero.setPasswordNuncaExpira(true);
            cajero.setDebeCambiarPassword(false);
            usuarioRepository.save(cajero);
        }

        adminToken = jwtService.generateToken("admin", "ADMIN");
        cajeroToken = jwtService.generateToken("cajero", "CAJERO");
    }

    private Map<String, Object> bodyRed() {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", "Caja 1");
        body.put("ip", "192.168.1.50");
        return body;
    }

    @Test
    @DisplayName("Petición sin token devuelve 401 Unauthorized")
    void listarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/impresoras")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Usuario no admin (ej. CAJERO) devuelve 403 Forbidden")
    void crearConUsuarioNoAdminDevuelve403() throws Exception {
        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + cajeroToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyRed())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CRUD completo de impresora RED con valores por defecto")
    void crudCompletoDeImpresoraRedConDefaults() throws Exception {
        String response = mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyRed())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Caja 1"))
                .andExpect(jsonPath("$.tipoConexion").value("RED"))
                .andExpect(jsonPath("$.puerto").value(9100))
                .andExpect(jsonPath("$.cola").value(Matchers.nullValue()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/impresoras/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Caja 1"));

        mockMvc.perform(put("/impresoras/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Caja 2", "ip", "192.168.1.51"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Caja 2"));

        mockMvc.perform(delete("/impresoras/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/impresoras/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Crear tipo CUPS sin cola devuelve 400 Bad Request")
    void crearTipoCupsSinColaDevuelve400() throws Exception {
        Map<String, Object> body = new HashMap<>(bodyRed());
        body.put("tipoConexion", "CUPS");

        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Crear tipo CUPS con cola usa puerto default 631")
    void crearTipoCupsConColaUsaPuertoDefault631() throws Exception {
        Map<String, Object> body = new HashMap<>(bodyRed());
        body.put("tipoConexion", "cups");
        body.put("cola", "TERMICA1");

        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoConexion").value("CUPS"))
                .andExpect(jsonPath("$.puerto").value(631))
                .andExpect(jsonPath("$.cola").value("TERMICA1"));
    }

    @Test
    @DisplayName("Crear tipo RED ignora cola enviada y guarda null")
    void crearTipoRedIgnoraColaEnviada() throws Exception {
        Map<String, Object> body = new HashMap<>(bodyRed());
        body.put("cola", "esto-se-ignora");

        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cola").value(Matchers.nullValue()));
    }

    @Test
    @DisplayName("Crear impresora con nombre duplicado devuelve 409 Conflict")
    void crearNombreDuplicadoDevuelve409() throws Exception {
        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyRed())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/impresoras")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bodyRed())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Eliminar impresora asignada a una ticketera devuelve 409 Conflict")
    void eliminarImpresoraEnUsoDevuelve409() throws Exception {
        Impresora imp = new Impresora(null, "Impresora Kiosco", TipoConexion.RED, "192.168.1.100", 9100, null);
        imp = impresoraRepository.save(imp);

        Ticketera ticketera = new Ticketera(null, "Ticketera Entrada", imp.getId());
        ticketeraRepository.save(ticketera);

        mockMvc.perform(delete("/impresoras/" + imp.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Imprimir en impresora inexistente devuelve 404 Not Found")
    void imprimirEnImpresoraInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/impresoras/999999/imprimir")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("texto", "hola"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Imprimir con conexión rechazada devuelve 503 en texto plano")
    void imprimirConConexionRechazadaDevuelve503() throws Exception {
        int puertoLibre;
        try (ServerSocket socket = new ServerSocket(0)) {
            puertoLibre = socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        Impresora impresora = new Impresora(null, "Caja rota", TipoConexion.RED, "127.0.0.1", puertoLibre, null);
        impresora = impresoraRepository.save(impresora);

        mockMvc.perform(post("/impresoras/" + impresora.getId() + "/imprimir")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("texto", "hola"))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("Imprimir con incluirLogo=true agrega comando raster ESC/POS si hay logo configurado")
    void imprimirConIncluirLogoAgregaElComandoRasterSiHayLogoConfigurado() throws Exception {
        Institucion institucion = new Institucion(1L, "Heladería Central", pngBase64DeDosPorDos());
        institucionRepository.save(institucion);

        byte[] recibido = imprimirYCapturar(Map.of("texto", "hola", "incluirLogo", true));

        assertTrue(contieneComandoRasterEscPos(recibido),
                "con incluirLogo=true y logo configurado debe mandarse el comando ESC/POS de imagen");
    }

    @Test
    @DisplayName("Imprimir sin incluirLogo no lo agrega aunque haya logo configurado")
    void imprimirSinIncluirLogoNoLoAgregaAunqueHayaUnoConfigurado() throws Exception {
        Institucion institucion = new Institucion(1L, "Heladería Central", pngBase64DeDosPorDos());
        institucionRepository.save(institucion);

        byte[] recibido = imprimirYCapturar(Map.of("texto", "hola"));

        assertFalse(contieneComandoRasterEscPos(recibido),
                "sin incluirLogo no debe mandarse comando de imagen");
    }

    @Test
    @DisplayName("Imprimir con incluirLogo=true sin logo configurado no rompe la impresión")
    void imprimirConIncluirLogoSinLogoConfiguradoNoRompeLaImpresion() throws Exception {
        institucionRepository.deleteAll();

        byte[] recibido = imprimirYCapturar(Map.of("texto", "hola", "incluirLogo", true));

        assertFalse(contieneComandoRasterEscPos(recibido));
        assertTrue(new String(recibido, StandardCharsets.ISO_8859_1).contains("hola"));
    }

    private byte[] imprimirYCapturar(Map<String, Object> body) throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            Impresora impresora = new Impresora(null, "Caja simulada", TipoConexion.RED, "127.0.0.1", servidor.getLocalPort(), null);
            impresora = impresoraRepository.save(impresora);

            AtomicReference<byte[]> recibido = new AtomicReference<>();
            CountDownLatch listo = new CountDownLatch(1);
            new Thread(() -> {
                try (Socket socket = servidor.accept()) {
                    recibido.set(socket.getInputStream().readAllBytes());
                } catch (IOException ignored) {
                } finally {
                    listo.countDown();
                }
            }).start();

            mockMvc.perform(post("/impresoras/" + impresora.getId() + "/imprimir")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk());

            assertTrue(listo.await(5, TimeUnit.SECONDS), "la impresora simulada no recibió la conexión");
            return recibido.get();
        }
    }

    private String pngBase64DeDosPorDos() throws IOException {
        BufferedImage imagen = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        imagen.setRGB(0, 0, 0x000000);
        imagen.setRGB(1, 0, 0xFFFFFF);
        imagen.setRGB(0, 1, 0x000000);
        imagen.setRGB(1, 1, 0xFFFFFF);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", png);
        return Base64.getEncoder().encodeToString(png.toByteArray());
    }

    private boolean contieneComandoRasterEscPos(byte[] datos) {
        if (datos == null) return false;
        byte[] comando = {0x1D, 0x76, 0x30, 0x00};
        outer:
        for (int i = 0; i <= datos.length - comando.length; i++) {
            for (int j = 0; j < comando.length; j++) {
                if (datos[i + j] != comando[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }
}
