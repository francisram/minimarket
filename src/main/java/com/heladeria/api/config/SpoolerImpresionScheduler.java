package com.heladeria.api.config;

import com.heladeria.api.services.TrabajoImpresionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Dispara el spooler de impresión periódicamente. Se desactiva con app.impresion.spooler.enabled=false (p. ej. en tests). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.impresion.spooler.enabled", havingValue = "true", matchIfMissing = true)
public class SpoolerImpresionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SpoolerImpresionScheduler.class);
    private final TrabajoImpresionService service;

    public SpoolerImpresionScheduler(TrabajoImpresionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${app.impresion.spooler.intervalo-ms:3000}", initialDelayString = "${app.impresion.spooler.intervalo-ms:3000}")
    public void procesar() {
        try {
            service.procesarPendientesAsync(20);
        } catch (Exception e) {
            log.error("Error inesperado en el spooler de impresión", e);
        }
    }
}
