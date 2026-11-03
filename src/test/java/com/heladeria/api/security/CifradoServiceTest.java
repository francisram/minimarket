package com.heladeria.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cifrado AES-256-GCM")
class CifradoServiceTest {

    private static final String CLAVE = Base64.getEncoder().encodeToString(new byte[32]).replace('A', 'Q');
    private static final String H2 = "jdbc:h2:mem:x";
    private static final String PG = "jdbc:postgresql://db:5432/heladeriadb";

    private CifradoService servicio(String clave) {
        return new CifradoService(clave, H2);
    }

    @Test
    @DisplayName("Cifra y descifra devolviendo el texto original, también con tildes y símbolos")
    void ida_y_vuelta() {
        CifradoService s = servicio(CLAVE);
        for (String texto : new String[]{"secreto", "Contraseña ñandú #2026!", "a".repeat(100)}) {
            String cifrado = s.cifrar(texto);
            assertTrue(cifrado.startsWith(CifradoService.PREFIJO));
            assertFalse(cifrado.contains(texto));
            assertEquals(texto, s.descifrar(cifrado));
        }
    }

    @Test
    @DisplayName("Cada cifrado usa un IV distinto: el mismo texto da resultados diferentes")
    void ivAleatorio() {
        CifradoService s = servicio(CLAVE);
        assertNotEquals(s.cifrar("igual"), s.cifrar("igual"));
    }

    @Test
    @DisplayName("Un dato alterado o descifrado con otra clave se detecta y falla")
    void detectaAlteracionYClaveIncorrecta() {
        CifradoService s = servicio(CLAVE);
        String cifrado = s.cifrar("secreto");
        char ultimo = cifrado.charAt(cifrado.length() - 5);
        String alterado = cifrado.substring(0, cifrado.length() - 5) + (ultimo == 'A' ? 'B' : 'A') + cifrado.substring(cifrado.length() - 4);
        assertThrows(IllegalStateException.class, () -> s.descifrar(alterado));

        CifradoService otra = servicio(Base64.getEncoder().encodeToString("otra-clave-de-32-bytes-exactos!!".getBytes()));
        assertThrows(IllegalStateException.class, () -> otra.descifrar(cifrado));
    }

    @Test
    @DisplayName("Nulos y vacíos se mantienen; un dato antiguo sin prefijo se lee tal cual; no se cifra dos veces")
    void bordes() {
        CifradoService s = servicio(CLAVE);
        assertNull(s.cifrar(null));
        assertNull(s.cifrar(""));
        assertNull(s.descifrar(null));
        assertEquals("texto-antiguo", s.descifrar("texto-antiguo"));
        String cifrado = s.cifrar("x");
        assertEquals(cifrado, s.cifrar(cifrado));
    }

    @Test
    @DisplayName("Con PostgreSQL la clave es obligatoria y debe ser de 32 bytes en base64; con H2 solo advierte")
    void validacionDeClave() {
        assertThrows(IllegalStateException.class, () -> new CifradoService("", PG));
        assertThrows(IllegalStateException.class, () -> new CifradoService("no-es-base64!!", PG));
        assertThrows(IllegalStateException.class, () -> new CifradoService(Base64.getEncoder().encodeToString(new byte[16]), PG));
        assertDoesNotThrow(() -> new CifradoService(CLAVE, PG));
        assertDoesNotThrow(() -> new CifradoService("", H2));
    }
}
