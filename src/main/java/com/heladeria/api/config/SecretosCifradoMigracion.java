package com.heladeria.api.config;

import com.heladeria.api.security.CifradoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Cifra al arrancar las contraseñas de impresoras que aún estén guardadas en texto plano (datos anteriores al cifrado). */
@Component
public class SecretosCifradoMigracion {

    private static final Logger log = LoggerFactory.getLogger(SecretosCifradoMigracion.class);
    private final JdbcTemplate jdbc;
    private final CifradoService cifrado;

    public SecretosCifradoMigracion(JdbcTemplate jdbc, CifradoService cifrado) {
        this.jdbc = jdbc;
        this.cifrado = cifrado;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        try {
            int n = cifrarPasswordsDeImpresoras();
            if (n > 0) {
                log.info("Se cifraron {} contraseña(s) de impresoras que estaban en texto plano.", n);
            }
        } catch (Exception e) {
            log.error("No se pudo cifrar las contraseñas de impresoras existentes: {}", e.getMessage());
        }
    }

    /** Usa SQL directo: la detección de cambios de JPA no vería diferencia entre el texto plano leído y el mismo texto. */
    public int cifrarPasswordsDeImpresoras() {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT id, password FROM impresoras WHERE password IS NOT NULL AND password <> '' AND password NOT LIKE ?",
                CifradoService.PREFIJO + "%");
        for (Map<String, Object> f : filas) {
            jdbc.update("UPDATE impresoras SET password = ? WHERE id = ?", cifrado.cifrar((String) f.get("password")), f.get("id"));
        }
        return filas.size();
    }
}
