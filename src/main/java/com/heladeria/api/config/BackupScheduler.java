package com.heladeria.api.config;

import com.heladeria.api.services.BackupAutomaticoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/** Programa los respaldos. Se desactiva con app.backup.programado.enabled=false (p. ej. en tests). */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.backup.programado.enabled", havingValue = "true", matchIfMissing = true)
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);
    private final BackupAutomaticoService service;

    public BackupScheduler(BackupAutomaticoService service) {
        this.service = service;
    }

    /** Por defecto todos los días a las 03:00 (hora de Asunción). */
    @Scheduled(cron = "${app.backup.cron:0 0 3 * * *}", zone = "America/Asuncion")
    public void respaldoDiario() {
        log.info("Iniciando respaldo automático programado...");
        service.ejecutarCiclo();
    }

    /** Recuperación: si el servidor estuvo apagado a la hora programada, respalda poco después de arrancar. */
    @Scheduled(initialDelayString = "${app.backup.catchup-delay-ms:120000}", fixedDelay = Long.MAX_VALUE)
    public void respaldoAlArrancarSiHaceFalta() {
        if (service.respaldoVencido()) {
            log.warn("El último respaldo está vencido: se ejecuta un respaldo de recuperación tras el arranque.");
            service.ejecutarCiclo();
        }
    }
}
