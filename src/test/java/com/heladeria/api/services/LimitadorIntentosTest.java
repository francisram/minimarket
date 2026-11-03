package com.heladeria.api.services;

import com.heladeria.api.exceptions.DemasiadosIntentosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Límite de intentos fallidos")
class LimitadorIntentosTest {

    private final LimitadorIntentos limitador = new LimitadorIntentos();
    private final Instant t0 = Instant.parse("2026-11-03T12:00:00Z");

    @Test
    @DisplayName("Bloquea recién al quinto fallo y avisa cuánto esperar")
    void bloqueaAlQuintoFallo() {
        for (int i = 0; i < 4; i++) {
            assertFalse(limitador.registrarFallo("anular:cajero1", t0.plusSeconds(i)));
            limitador.verificarNoBloqueado("anular:cajero1", t0.plusSeconds(i));
        }
        assertTrue(limitador.registrarFallo("anular:cajero1", t0.plusSeconds(5)));

        DemasiadosIntentosException e = assertThrows(DemasiadosIntentosException.class,
                () -> limitador.verificarNoBloqueado("anular:cajero1", t0.plusSeconds(10)));
        assertTrue(e.getMessage().contains("minuto"));
    }

    @Test
    @DisplayName("El bloqueo expira y los contadores son independientes por usuario")
    void bloqueoExpiraYEsPorUsuario() {
        for (int i = 0; i < 5; i++) limitador.registrarFallo("anular:cajero1", t0);

        assertDoesNotThrow(() -> limitador.verificarNoBloqueado("anular:cajero2", t0));
        assertThrows(DemasiadosIntentosException.class, () -> limitador.verificarNoBloqueado("ANULAR:Cajero1", t0.plusSeconds(60)));
        assertDoesNotThrow(() -> limitador.verificarNoBloqueado("anular:cajero1", t0.plusSeconds(16 * 60)));
    }

    @Test
    @DisplayName("Un acierto reinicia el contador y los fallos viejos fuera de la ventana no suman")
    void exitoYVentana() {
        for (int i = 0; i < 4; i++) limitador.registrarFallo("k", t0);
        limitador.registrarExito("k");
        assertFalse(limitador.registrarFallo("k", t0.plusSeconds(1)), "Tras un acierto se empieza de cero");

        for (int i = 0; i < 3; i++) limitador.registrarFallo("v", t0);
        assertFalse(limitador.registrarFallo("v", t0.plusSeconds(20 * 60)), "Fuera de la ventana de 15 min se reinicia");
        assertFalse(limitador.registrarFallo("v", t0.plusSeconds(20 * 60 + 1)));
    }
}
