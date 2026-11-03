package com.heladeria.api.services;

import com.heladeria.api.exceptions.DemasiadosIntentosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Límite de intentos fallidos por clave (p. ej. quien prueba contraseñas de login o claves de supervisor).
 * Tras {@code maxFallos} fallos dentro de la ventana, la clave queda bloqueada un tiempo.
 * Los contadores viven en la base de datos: sobreviven a un reinicio y los comparten varias instancias.
 */
@Component
public class LimitadorIntentos {

    private final IntentosFallidosStore store;

    @Value("${app.seguridad.intentos.max-fallos:5}")
    private int maxFallos = 5;

    @Value("${app.seguridad.intentos.ventana-minutos:15}")
    private long ventanaMinutos = 15;

    @Value("${app.seguridad.intentos.bloqueo-minutos:15}")
    private long bloqueoMinutos = 15;

    @Autowired
    LimitadorIntentos(IntentosFallidosStore store) {
        this.store = store;
    }

    public void verificarNoBloqueado(String clave) {
        verificarNoBloqueado(clave, Instant.now());
    }

    public void verificarNoBloqueado(String clave, Instant ahora) {
        store.bloqueadoHasta(normalizar(clave)).ifPresent(hasta -> {
            if (hasta != null && ahora.isBefore(hasta)) {
                long minutos = Math.max(1, Duration.between(ahora, hasta).toMinutes() + 1);
                throw new DemasiadosIntentosException("Demasiados intentos fallidos. Intente nuevamente en " + minutos + " minuto(s).");
            }
        });
    }

    /** Registra un fallo. Devuelve true si con este fallo la clave quedó bloqueada. */
    public boolean registrarFallo(String clave) {
        return registrarFallo(clave, Instant.now());
    }

    public boolean registrarFallo(String clave, Instant ahora) {
        String k = normalizar(clave);
        Duration ventana = Duration.ofMinutes(ventanaMinutos);
        Duration bloqueo = Duration.ofMinutes(bloqueoMinutos);
        // Varios primeros fallos simultáneos de una clave nueva: los perdedores reintentan y ya encuentran la fila
        for (int intento = 1; ; intento++) {
            try {
                return store.registrarFallo(k, ahora, maxFallos, ventana, bloqueo);
            } catch (DataIntegrityViolationException | jakarta.persistence.PersistenceException carrera) {
                if (intento >= 5) {
                    throw carrera;
                }
            }
        }
    }

    public void registrarExito(String clave) {
        try {
            store.borrar(normalizar(clave));
        } catch (org.springframework.dao.EmptyResultDataAccessException ignorada) {
            // no había contador
        }
    }

    private static String normalizar(String clave) {
        String k = clave == null ? "" : clave.trim().toLowerCase(Locale.ROOT);
        return k.length() > 150 ? k.substring(0, 150) : k;
    }
}
