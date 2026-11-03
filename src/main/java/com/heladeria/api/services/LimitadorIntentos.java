package com.heladeria.api.services;

import com.heladeria.api.exceptions.DemasiadosIntentosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Límite de intentos fallidos por clave (p. ej. el cajero que prueba claves de supervisor).
 * Tras {@code maxFallos} fallos dentro de la ventana, la clave queda bloqueada un tiempo.
 * El estado vive en memoria: se reinicia con la aplicación, y asume una sola instancia.
 */
@Component
public class LimitadorIntentos {

    private record Registro(int fallos, Instant primerFallo, Instant bloqueadoHasta) { }

    private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

    @Value("${app.seguridad.intentos.max-fallos:5}")
    private int maxFallos = 5;

    @Value("${app.seguridad.intentos.ventana-minutos:15}")
    private long ventanaMinutos = 15;

    @Value("${app.seguridad.intentos.bloqueo-minutos:15}")
    private long bloqueoMinutos = 15;

    public void verificarNoBloqueado(String clave) {
        verificarNoBloqueado(clave, Instant.now());
    }

    public void verificarNoBloqueado(String clave, Instant ahora) {
        Registro r = registros.get(normalizar(clave));
        if (r != null && r.bloqueadoHasta() != null && ahora.isBefore(r.bloqueadoHasta())) {
            long minutos = Math.max(1, Duration.between(ahora, r.bloqueadoHasta()).toMinutes() + 1);
            throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intente nuevamente en " + minutos + " minuto(s).");
        }
    }

    /** Registra un fallo. Devuelve true si con este fallo la clave quedó bloqueada. */
    public boolean registrarFallo(String clave) {
        return registrarFallo(clave, Instant.now());
    }

    public boolean registrarFallo(String clave, Instant ahora) {
        boolean[] bloqueada = {false};
        registros.compute(normalizar(clave), (k, actual) -> {
            Instant inicio = ahora;
            int fallos = 1;
            if (actual != null && actual.primerFallo() != null
                    && ahora.isBefore(actual.primerFallo().plus(Duration.ofMinutes(ventanaMinutos)))) {
                inicio = actual.primerFallo();
                fallos = actual.fallos() + 1;
            }
            if (fallos >= maxFallos) {
                bloqueada[0] = true;
                return new Registro(0, null, ahora.plus(Duration.ofMinutes(bloqueoMinutos)));
            }
            return new Registro(fallos, inicio, null);
        });
        return bloqueada[0];
    }

    public void registrarExito(String clave) {
        registros.remove(normalizar(clave));
    }

    private static String normalizar(String clave) {
        return clave == null ? "" : clave.trim().toLowerCase(Locale.ROOT);
    }
}
