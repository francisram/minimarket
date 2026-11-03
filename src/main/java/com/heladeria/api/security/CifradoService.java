package com.heladeria.api.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado reversible AES-256-GCM para secretos que la aplicación debe recuperar (p. ej. la clave de una impresora de red).
 * Formato guardado: "enc:v1:" + base64(IV de 12 bytes || texto cifrado || etiqueta de autenticación).
 * Cada cifrado usa un IV aleatorio, y GCM detecta cualquier alteración del dato.
 * La clave (APP_CIFRADO_CLAVE, 32 bytes en base64) vive fuera de la base: un respaldo o volcado solo no revela los secretos.
 */
@Service
public class CifradoService {

    public static final String PREFIJO = "enc:v1:";
    private static final Logger log = LoggerFactory.getLogger(CifradoService.class);
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec clave;
    private final SecureRandom azar = new SecureRandom();

    public CifradoService(@Value("${app.seguridad.clave-cifrado:}") String claveBase64,
                          @Value("${spring.datasource.url:}") String datasourceUrl) {
        boolean produccion = datasourceUrl != null && datasourceUrl.startsWith("jdbc:postgresql");
        if (claveBase64 == null || claveBase64.isBlank()) {
            if (produccion) {
                throw new IllegalStateException("SEGURIDAD: falta APP_CIFRADO_CLAVE, necesaria para cifrar secretos en la base. "
                        + "Genere una con: openssl rand -base64 32");
            }
            log.warn("SEGURIDAD: APP_CIFRADO_CLAVE no definida; se usa una clave de desarrollo (modo desarrollo: solo advertencia).");
            this.clave = new SecretKeySpec(sha256("clave-de-desarrollo-no-usar-en-produccion"), "AES");
            return;
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(claveBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("APP_CIFRADO_CLAVE no es base64 válido. Genere una con: openssl rand -base64 32");
        }
        if (bytes.length != 32) {
            throw new IllegalStateException("APP_CIFRADO_CLAVE debe tener exactamente 32 bytes (AES-256) y tiene " + bytes.length
                    + ". Genere una con: openssl rand -base64 32");
        }
        this.clave = new SecretKeySpec(bytes, "AES");
    }

    public boolean estaCifrado(String valor) {
        return valor != null && valor.startsWith(PREFIJO);
    }

    /** Devuelve null para null o vacío; un valor ya cifrado no se vuelve a cifrar. */
    public String cifrar(String textoPlano) {
        if (textoPlano == null || textoPlano.isEmpty()) {
            return null;
        }
        if (estaCifrado(textoPlano)) {
            return textoPlano;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            azar.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, clave, new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            ByteBuffer salida = ByteBuffer.allocate(iv.length + cifrado.length);
            salida.put(iv).put(cifrado);
            return PREFIJO + Base64.getEncoder().encodeToString(salida.array());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar el valor", e);
        }
    }

    /**
     * Descifra un valor guardado. Un valor sin prefijo se considera un dato antiguo sin cifrar y se devuelve tal cual
     * (se cifra al arrancar). Lanza excepción si la clave no corresponde o el dato fue alterado.
     */
    public String descifrar(String guardado) {
        if (guardado == null || guardado.isEmpty()) {
            return null;
        }
        if (!estaCifrado(guardado)) {
            return guardado;
        }
        try {
            byte[] datos = Base64.getDecoder().decode(guardado.substring(PREFIJO.length()));
            ByteBuffer buf = ByteBuffer.wrap(datos);
            byte[] iv = new byte[IV_BYTES];
            buf.get(iv);
            byte[] cifrado = new byte[buf.remaining()];
            buf.get(cifrado);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, clave, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descifrar el valor: la clave no corresponde o el dato fue alterado", e);
        }
    }

    private static byte[] sha256(String texto) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
