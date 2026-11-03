package com.heladeria.api.config;

import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.services.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Con PostgreSQL, al arrancar marca para cambio obligatorio a todo usuario activo cuya contraseña siga siendo una de las
 * conocidas de demostración. La comparación es local contra el hash guardado; nunca se escriben ni se registran claves.
 */
@Component
public class ClavesPorDefectoCheck {

    private static final Logger log = LoggerFactory.getLogger(ClavesPorDefectoCheck.class);
    /** Contraseñas de demostración conocidas: no se aceptan como contraseña nueva. */
    public static final List<String> CLAVES_CONOCIDAS = List.of(
            "admin123", "owner123", "CambiarEstaPasswordAdmin123!", "CambiarEstaPasswordOwner123!");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    private final boolean produccion;

    public ClavesPorDefectoCheck(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                 AuditoriaService auditoriaService,
                                 @Value("${spring.datasource.url:}") String datasourceUrl) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
        this.produccion = datasourceUrl != null && datasourceUrl.startsWith("jdbc:postgresql");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        if (!produccion) {
            return;
        }
        try {
            marcarUsuariosConClaveConocida();
        } catch (Exception e) {
            log.error("No se pudo revisar las contraseñas por defecto: {}", e.getMessage());
        }
    }

    /** Devuelve cuántos usuarios quedaron obligados a cambiar su contraseña. */
    @Transactional
    public int marcarUsuariosConClaveConocida() {
        int marcados = 0;
        for (Usuario u : usuarioRepository.findAll()) {
            if (!Boolean.TRUE.equals(u.getEstado()) || Boolean.TRUE.equals(u.getDebeCambiarPassword())
                    || u.getPasswordHash() == null) {
                continue;
            }
            for (String clave : CLAVES_CONOCIDAS) {
                if (passwordEncoder.matches(clave, u.getPasswordHash())) {
                    u.setDebeCambiarPassword(true);
                    usuarioRepository.save(u);
                    marcados++;
                    log.warn("SEGURIDAD: el usuario '{}' tiene una contraseña de demostración; deberá cambiarla en su próximo ingreso.", u.getUsername());
                    auditoriaService.registrarLog("sistema", "SEGURIDAD", "ALERTA", u.getId(), u.getUsername(),
                            "Contraseña de demostración detectada: se exige cambio en el próximo ingreso.", "localhost");
                    break;
                }
            }
        }
        return marcados;
    }
}
