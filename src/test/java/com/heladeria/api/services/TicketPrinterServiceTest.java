package com.heladeria.api.services;

import com.heladeria.api.dto.TicketEmitidoDTO;
import com.heladeria.api.entities.*;
import com.heladeria.api.entities.enums.*;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.repositories.InstitucionRepository;
import com.heladeria.api.repositories.TicketeraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Pruebas del servicio de emisión e impresión de tickets TicketPrinterService")
class TicketPrinterServiceTest {

    private static final byte[] COMANDO_RASTER_ESC_POS = {0x1D, 0x76, 0x30, 0x00};

    @Autowired
    private TicketPrinterService ticketPrinterService;

    @Autowired
    private TicketeraRepository ticketeraRepository;

    @Autowired
    private ImpresoraRepository impresoraRepository;

    @Autowired
    private InstitucionRepository institucionRepository;

    @BeforeEach
    void setUp() {
        ticketeraRepository.deleteAll();
        impresoraRepository.deleteAll();
        institucionRepository.deleteAll();
    }

    private Long crearImpresoraRed(int puerto) {
        Impresora impresora = new Impresora(null, "EPSON-TM88-" + puerto, TipoConexion.RED, "127.0.0.1", puerto, null);
        return impresoraRepository.save(impresora).getId();
    }

    private void crearInstitucionConLogo(String logoBase64) {
        Institucion institucion = new Institucion(1L, "Heladería Central", logoBase64);
        institucionRepository.save(institucion);
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
        outer:
        for (int i = 0; i <= datos.length - COMANDO_RASTER_ESC_POS.length; i++) {
            for (int j = 0; j < COMANDO_RASTER_ESC_POS.length; j++) {
                if (datos[i + j] != COMANDO_RASTER_ESC_POS[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private byte[] emitirEImprimir(TicketEmitidoDTO ticket) throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            Long impresoraId = crearImpresoraRed(servidor.getLocalPort());
            Ticketera ticketera = new Ticketera(null, "Ticketera Entrada", impresoraId);
            Long id = ticketeraRepository.save(ticketera).getId();

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

            ticketPrinterService.imprimirEnSegundoPlano(id, ticket);

            assertTrue(listo.await(5, TimeUnit.SECONDS), "la impresora simulada no recibió la conexión a tiempo");
            return recibido.get();
        }
    }

    @Test
    @DisplayName("Envía el ticket formateado en ESC/POS a la impresora de la ticketera")
    void enviaElTicketPorEscPosALaImpresoraDeLaTicketera() throws Exception {
        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));

        String texto = new String(recibido, StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("HEL-1"), "el ticket impreso debe incluir el código: " + texto);
        assertTrue(texto.contains("Mostrador 1"), "el ticket impreso debe incluir el lugar: " + texto);
    }

    @Test
    @DisplayName("Ticket preferencial imprime la leyenda ** PREFERENCIAL **")
    void elTicketPreferencialLoIndicaEnElPapel() throws Exception {
        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, true, OffsetDateTime.now()));

        assertTrue(new String(recibido, StandardCharsets.ISO_8859_1).contains("PREFERENCIAL"));
    }

    @Test
    @DisplayName("Ticket común no imprime la leyenda PREFERENCIAL")
    void elTicketComunNoDiceNadaDePreferencial() throws Exception {
        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));

        assertFalse(new String(recibido, StandardCharsets.ISO_8859_1).contains("PREFERENCIAL"));
    }

    @Test
    @DisplayName("Imprime el logo de la institución como cabecera si está configurado")
    void imprimeElLogoDeLaInstitucionComoCabeceraSiEstaConfigurado() throws Exception {
        crearInstitucionConLogo(pngBase64DeDosPorDos());

        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));

        assertTrue(contieneComandoRasterEscPos(recibido),
                "el ticket impreso debe incluir el comando ESC/POS de imagen raster con el logo");
    }

    @Test
    @DisplayName("No imprime comando de logo si no hay ninguno configurado")
    void noImprimeCabeceraDeLogoSiNoHayNingunoConfigurado() throws Exception {
        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));

        assertFalse(contieneComandoRasterEscPos(recibido),
                "sin logo configurado no debe mandarse el comando de imagen");
    }

    @Test
    @DisplayName("Logo inválido no rompe la impresión del resto del ticket")
    void logoInvalidoNoRompeLaImpresionDelRestoDelTicket() throws Exception {
        crearInstitucionConLogo(Base64.getEncoder().encodeToString("esto no es una imagen".getBytes(StandardCharsets.UTF_8)));

        byte[] recibido = emitirEImprimir(new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));

        String texto = new String(recibido, StandardCharsets.ISO_8859_1);
        assertTrue(texto.contains("HEL-1"), "un logo inválido no debe impedir imprimir el resto del ticket: " + texto);
        assertFalse(contieneComandoRasterEscPos(recibido));
    }

    @Test
    @DisplayName("ticketeraId nulo no hace nada ni lanza excepción")
    void ticketeraIdNuloNoHaceNadaNiLanzaExcepcion() {
        ticketPrinterService.imprimirEnSegundoPlano(null,
                new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));
    }

    @Test
    @DisplayName("ticketera inexistente no lanza excepción")
    void ticketeraInexistenteNoLanzaExcepcion() throws Exception {
        ticketPrinterService.imprimirEnSegundoPlano(999999L,
                new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));
        Thread.sleep(300);
    }

    @Test
    @DisplayName("impresora inalcanzable no lanza excepción en el hilo llamador")
    void impresoraInalcanzableNoLanzaExcepcion() throws Exception {
        Long impresoraId = crearImpresoraRed(1); // puerto sin listener
        Ticketera ticketera = new Ticketera(null, "Ticketera Fantasma", impresoraId);
        Long id = ticketeraRepository.save(ticketera).getId();

        ticketPrinterService.imprimirEnSegundoPlano(id,
                new TicketEmitidoDTO("HEL-1", "Mostrador 1", "1234567", false, false, OffsetDateTime.now()));
        Thread.sleep(300);
    }

    @Test
    @DisplayName("construirTicketPedido genera ticket ESC/POS con moneda Gs. sin decimales, desglose IVA y sin símbolo unicode ₲")
    void testConstruirTicketPedido() {
        Presentacion pres = Presentacion.builder()
                .id(1L)
                .nombre("Cucurucho 1 Bocha")
                .precio(new BigDecimal("12000"))
                .maxSabores(1)
                .build();

        Sabor ddl = Sabor.builder()
                .id(1L)
                .nombre("Dulce de Leche")
                .build();

        Topping banio = Topping.builder()
                .id(1L)
                .nombre("Baño de Chocolate")
                .precioExtra(new BigDecimal("3000"))
                .build();

        DetallePedido detalle = DetallePedido.builder()
                .tipoItem(TipoItemPedido.HELADO)
                .presentacion(pres)
                .sabores(List.of(ddl))
                .toppings(List.of(banio))
                .cantidad(2)
                .precioUnitario(new BigDecimal("15000"))
                .subtotal(new BigDecimal("30000"))
                .build();

        Pedido pedido = Pedido.builder()
                .id(101L)
                .clienteNombre("Juan Pérez")
                .metodoPago(MetodoPago.EFECTIVO)
                .tipoEntrega(TipoEntrega.MOSTRADOR)
                .estado(EstadoPedido.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .total(new BigDecimal("30000"))
                .detalles(List.of(detalle))
                .notas("Servir rápido")
                .build();

        byte[] ticketBytes = ticketPrinterService.construirTicketPedido(pedido);
        assertNotNull(ticketBytes);
        assertTrue(ticketBytes.length > 0);

        String texto = new String(ticketBytes, StandardCharsets.ISO_8859_1);

        // Validaciones clave de moneda y formato
        assertTrue(texto.contains("PEDIDO #101"));
        assertTrue(texto.contains("Cliente: Juan Pérez"));
        assertTrue(texto.contains("Pago: EFECTIVO"));
        assertTrue(texto.contains("Cucurucho 1 Bocha"));
        assertTrue(texto.contains("Dulce de Leche"));
        assertTrue(texto.contains("Baño de Chocolate"));
        assertTrue(texto.contains("Gs. 30.000"));
        assertTrue(texto.contains("TOTAL:"));

        // IVA 10% de 30.000 = 2.727, Gravadas = 27.273
        assertTrue(texto.contains("LIQUIDACION DE IVA"));
        assertTrue(texto.contains("Gravadas 10%:"));
        assertTrue(texto.contains("Gs. 27.273"));
        assertTrue(texto.contains("IVA 10%:"));
        assertTrue(texto.contains("Gs. 2.727"));

        // Asegurar que NO contenga el carácter unicode ₲
        assertFalse(texto.contains("\u20B2"), "El ticket térmico ESC/POS nunca debe contener el carácter unicode ₲");
    }
}
