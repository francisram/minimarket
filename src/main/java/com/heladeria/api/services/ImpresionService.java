package com.heladeria.api.services;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.TipoConexion;
import com.heladeria.api.exceptions.ImpresionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ImpresionService {

    private static final Logger log = LoggerFactory.getLogger(ImpresionService.class);

    private static final int TIMEOUT_MS = 4000;
    private static final Charset CHARSET_IMPRESION = Charset.forName("windows-1252");

    // ESC @ (inicializar) + ESC t 16 (selección de página de códigos WPC1252 para ñ y acentos)
    private static final byte[] INICIALIZAR = {0x1B, 0x40, 0x1B, 0x74, 0x10};
    private static final byte[] AVANCE_PAPEL = "\n\n\n\n".getBytes(CHARSET_IMPRESION);
    private static final byte[] CORTAR = {0x1D, 0x56, 0x00};

    // 384 dots = 58mm a 203dpi, el ancho de papel térmico más común
    private static final int LOGO_MAX_ANCHO_DOTS = 384;

    // Luces altas a blanco: umbral para evitar que el tramado llene el fondo de puntos sueltos
    private static final double LOGO_PUNTO_BLANCO = 205;

    public void imprimir(Impresora impresora, String texto) {
        imprimir(impresora, construirJob(texto));
    }

    public void imprimir(Impresora impresora, String texto, String logoBase64) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logoRaster(logoBase64).ifPresent(out::writeBytes);
        out.writeBytes(construirJob(texto));
        imprimir(impresora, out.toByteArray());
    }

    public void imprimir(Impresora impresora, byte[] job) {
        if (impresora.getTipoConexion() == TipoConexion.CUPS) {
            imprimirPorCups(impresora, job);
        } else {
            imprimirPorRed(impresora, job);
        }
    }

    private byte[] construirJob(String texto) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(INICIALIZAR);
        out.writeBytes((texto != null ? texto : "").getBytes(CHARSET_IMPRESION));
        out.writeBytes(AVANCE_PAPEL);
        out.writeBytes(CORTAR);
        return out.toByteArray();
    }

    public Optional<byte[]> logoRaster(String logoBase64) {
        if (!StringUtils.hasText(logoBase64)) {
            return Optional.empty();
        }
        try {
            byte[] crudo = Base64.getDecoder().decode(logoBase64);
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(crudo));
            if (imagen == null) {
                log.warn("El logo no es una imagen reconocible, se omite de la impresión");
                return Optional.empty();
            }
            return Optional.of(rasterEscPos(escalarSiHaceFalta(imagen)));
        } catch (Exception e) {
            log.warn("No se pudo decodificar el logo, se omite de la impresión: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private BufferedImage escalarSiHaceFalta(BufferedImage original) {
        if (original.getWidth() <= LOGO_MAX_ANCHO_DOTS) {
            return original;
        }
        int alturaEscalada = Math.round(original.getHeight() * (LOGO_MAX_ANCHO_DOTS / (float) original.getWidth()));
        BufferedImage escalada = new BufferedImage(LOGO_MAX_ANCHO_DOTS, alturaEscalada, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = escalada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, LOGO_MAX_ANCHO_DOTS, alturaEscalada, null);
        g.dispose();
        return escalada;
    }

    private byte[] rasterEscPos(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        int bytesPorFila = (ancho + 7) / 8;
        boolean[][] negro = tramar(imagen);

        ByteArrayOutputStream datos = new ByteArrayOutputStream();
        for (int y = 0; y < alto; y++) {
            for (int xByte = 0; xByte < bytesPorFila; xByte++) {
                int b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = xByte * 8 + bit;
                    if (x < ancho && negro[y][x]) {
                        b |= 0x80 >> bit;
                    }
                }
                datos.write(b);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{0x1D, 0x76, 0x30, 0x00});
        out.write(bytesPorFila & 0xFF);
        out.write((bytesPorFila >> 8) & 0xFF);
        out.write(alto & 0xFF);
        out.write((alto >> 8) & 0xFF);
        out.writeBytes(datos.toByteArray());
        out.writeBytes("\n".getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    private boolean[][] tramar(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();
        double[][] luz = new double[alto][ancho];
        double min = 255;
        double max = 0;
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int argb = imagen.getRGB(x, y);
                double opacidad = ((argb >>> 24) & 0xFF) / 255.0;
                double gris = 0.299 * ((argb >>> 16) & 0xFF) + 0.587 * ((argb >>> 8) & 0xFF) + 0.114 * (argb & 0xFF);
                double valor = opacidad * gris + (1 - opacidad) * 255;
                luz[y][x] = valor;
                min = Math.min(min, valor);
                max = Math.max(max, valor);
            }
        }

        double rango = max - min;
        if (rango >= 16) {
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {
                    double estirado = (luz[y][x] - min) / rango * 255;
                    luz[y][x] = Math.min(255, estirado * 255 / LOGO_PUNTO_BLANCO);
                }
            }
        }

        boolean[][] negro = new boolean[alto][ancho];
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                double viejo = luz[y][x];
                double nuevo = viejo < 128 ? 0 : 255;
                negro[y][x] = nuevo == 0;
                double error = viejo - nuevo;
                if (x + 1 < ancho) {
                    luz[y][x + 1] += error * 7 / 16;
                }
                if (y + 1 < alto) {
                    if (x > 0) {
                        luz[y + 1][x - 1] += error * 3 / 16;
                    }
                    luz[y + 1][x] += error * 5 / 16;
                    if (x + 1 < ancho) {
                        luz[y + 1][x + 1] += error / 16;
                    }
                }
            }
        }
        return negro;
    }

    private void imprimirPorRed(Impresora impresora, byte[] job) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(impresora.getIp(), impresora.getPuerto()), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            socket.getOutputStream().write(job);
            socket.getOutputStream().flush();
        } catch (SocketTimeoutException e) {
            throw new ImpresionException(errorRed(impresora, "tiempo de espera agotado"));
        } catch (ConnectException e) {
            throw new ImpresionException(errorRed(impresora, "conexión rechazada"));
        } catch (IOException e) {
            throw new ImpresionException(errorRed(impresora, e.getMessage()));
        }
    }

    private String errorRed(Impresora impresora, String detalle) {
        return "No se pudo imprimir en " + impresora.getNombre() + " (" + impresora.getIp() + ":"
                + impresora.getPuerto() + "): " + detalle;
    }

    private void imprimirPorCups(Impresora impresora, byte[] job) {
        String host = impresora.getIp() + ":" + impresora.getPuerto();
        ProcessBuilder pb = new ProcessBuilder("lp", "-h", host, "-d", impresora.getCola(), "-o", "raw");
        pb.redirectErrorStream(true);

        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new ImpresionException("No se pudo ejecutar lp para " + impresora.getNombre() + ": " + e.getMessage());
        }

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Thread drenador = Thread.ofVirtual().start(() -> {
            try (InputStream in = process.getInputStream()) {
                in.transferTo(salida);
            } catch (IOException ignored) {
            }
        });

        try {
            process.getOutputStream().write(job);
            process.getOutputStream().flush();
        } catch (IOException ignored) {
        } finally {
            try {
                process.getOutputStream().close();
            } catch (IOException ignored) {
            }
        }

        boolean termino;
        try {
            termino = process.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new ImpresionException("Impresión interrumpida para " + impresora.getNombre());
        }

        if (!termino) {
            process.destroyForcibly();
            throw new ImpresionException("No se pudo imprimir en " + impresora.getNombre() + ": tiempo de espera agotado");
        }

        try {
            drenador.join(TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String mensaje = salida.toString(StandardCharsets.UTF_8).trim();
            throw new ImpresionException("lp terminó con código " + exitCode + " para " + impresora.getNombre()
                    + (mensaje.isEmpty() ? "" : ": " + mensaje));
        }
    }
}
