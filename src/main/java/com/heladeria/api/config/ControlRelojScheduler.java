package com.heladeria.api.config;

import com.heladeria.api.services.ControlRelojService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Registra la fecha vista al arrancar y cada 5 minutos. Se desactiva con app.licencia.control-reloj.enabled=false. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.licencia.control-reloj.enabled", havingValue = "true", matchIfMissing = true)
public class ControlRelojScheduler {

    private static final Logger log = LoggerFactory.getLogger(ControlRelojScheduler.class);
    private final ControlRelojService service;

    public ControlRelojScheduler(ControlRelojService service) {
        this.service = service;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        registrar();
    }

    @Scheduled(fixedDelay = 300_000, initialDelay = 300_000)
    public void periodico() {
        registrar();
    }

    private void registrar() {
        try {
            service.registrar();
        } catch (Exception e) {
            log.error("No se pudo registrar el control de reloj: {}", e.getMessage());
        }
    }
}
