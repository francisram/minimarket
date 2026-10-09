package com.heladeria.api.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de procesamiento y tramado ESC/POS en ImpresionService")
class ImpresionServiceTest {

    private static final int CABECERA = 8;

    private final ImpresionService servicio = new ImpresionService();

    private String pngGris(int[] grisPorColumna, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(grisPorColumna.length, alto, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < grisPorColumna.length; x++) {
                imagen.getRaster().setSample(x, y, 0, grisPorColumna[x]);
            }
        }
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", png);
        return Base64.getEncoder().encodeToString(png.toByteArray());
    }

    private int[] columnas(int cuantas, int gris) {
        int[] g = new int[cuantas];
        java.util.Arrays.fill(g, gris);
        return g;
    }

    private byte[] datos(byte[] comando) {
        byte[] datos = new byte[comando.length - CABECERA - 1];
        System.arraycopy(comando, CABECERA, datos, 0, datos.length);
        return datos;
    }

    @Test
    @DisplayName("Logo de grises claros produce puntos negros y fondo blanco limpio")
    void unLogoDeGrisesClarosImprimePuntosNegrosEnVezDeSalirEnBlanco() throws Exception {
        int[] grises = new int[24];
        for (int x = 0; x < 24; x++) {
            grises[x] = x < 8 ? 140 : x < 16 ? 200 : 255;
        }

        Optional<byte[]> comando = servicio.logoRaster(pngGris(grises, 16));

        byte[] datos = datos(comando.orElseThrow());
        assertEquals(16 * 3, datos.length);
        for (int fila = 0; fila < 16; fila++) {
            assertEquals((byte) 0xFF, datos[fila * 3], "el tono más oscuro debe salir negro");
            assertEquals((byte) 0x00, datos[fila * 3 + 2], "el fondo blanco debe quedar sin puntos");
        }
        int puntosDelTonoMedio = 0;
        for (int fila = 0; fila < 16; fila++) {
            puntosDelTonoMedio += Integer.bitCount(datos[fila * 3 + 1] & 0xFF);
        }
        assertTrue(puntosDelTonoMedio > 0 && puntosDelTonoMedio < 16 * 8,
                "el gris intermedio debe salir como trama, no todo negro ni todo blanco: " + puntosDelTonoMedio);
    }

    @Test
    @DisplayName("Fondo casi blanco (245) se mantiene sin puntos (luces altas a blanco)")
    void elFondoCasiBlancoNoSeLlenaDePuntosSueltos() throws Exception {
        int[] grises = new int[24];
        for (int x = 0; x < 24; x++) {
            grises[x] = x < 8 ? 130 : x < 16 ? 245 : 255;
        }

        byte[] datos = datos(servicio.logoRaster(pngGris(grises, 32)).orElseThrow());

        for (int fila = 0; fila < 32; fila++) {
            assertEquals((byte) 0xFF, datos[fila * 3]);
            assertEquals((byte) 0x00, datos[fila * 3 + 1], "un blanco de 245/255 es papel, no debe llevar puntos");
            assertEquals((byte) 0x00, datos[fila * 3 + 2]);
        }
    }

    @Test
    @DisplayName("Imagen totalmente blanca no genera ningún punto negro")
    void unaImagenTotalmenteBlancaNoImprimeNingunPunto() throws Exception {
        byte[] datos = datos(servicio.logoRaster(pngGris(columnas(16, 255), 8)).orElseThrow());

        for (byte b : datos) {
            assertEquals(0, b);
        }
    }

    @Test
    @DisplayName("Logo en blanco y negro puro queda idéntico")
    void unLogoEnBlancoYNegroPuroQuedaExactamenteIgual() throws Exception {
        byte[] datos = datos(servicio.logoRaster(pngGris(new int[]{0, 255}, 2)).orElseThrow());

        assertArrayEquals(new byte[]{(byte) 0x80, (byte) 0x80}, datos);
    }

    @Test
    @DisplayName("Cabecera GS v 0 trae ancho en bytes y alto en dots correctos")
    void laCabeceraIndicaAnchoEnBytesYAltoEnDots() throws Exception {
        byte[] comando = servicio.logoRaster(pngGris(columnas(24, 0), 5)).orElseThrow();

        assertArrayEquals(new byte[]{0x1D, 0x76, 0x30, 0x00, 3, 0, 5, 0}, java.util.Arrays.copyOf(comando, CABECERA));
    }

    @Test
    @DisplayName("Logo null, en blanco o inválido devuelve Optional.empty sin lanzar error")
    void sinLogoOConUnLogoInvalidoDevuelveVacio() {
        assertTrue(servicio.logoRaster(null).isEmpty());
        assertTrue(servicio.logoRaster("   ").isEmpty());
        assertTrue(servicio.logoRaster(Base64.getEncoder().encodeToString("no es una imagen".getBytes())).isEmpty());
    }
}
