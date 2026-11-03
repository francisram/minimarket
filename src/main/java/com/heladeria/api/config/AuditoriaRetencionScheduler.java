package com.heladeria.api.config;

import com.heladeria.api.services.AuditoriaRetencionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Aplica la retención de la auditoría una vez al día. Se desactiva con app.auditoria.retencion.enabled=false. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.auditoria.retencion.enabled", havingValue = "true", matchIfMissing = true)
public class AuditoriaRetencionScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaRetencionScheduler.class);
    private final AuditoriaRetencionService service;

    public AuditoriaRetencionScheduler(AuditoriaRetencionService service) {
        this.service = service;
    }

    /** Por defecto a las 03:30, después del respaldo de las 03:00 (así el respaldo ya incluye lo que se archiva). */
    @Scheduled(cron = "${app.auditoria.cron:0 30 3 * * *}", zone = "America/Asuncion")
    public void depurar() {
        try {
            service.aplicar();
        } catch (Exception e) {
            log.error("Falló la retención de la auditoría: {}", e.getMessage());
        }
    }
}
