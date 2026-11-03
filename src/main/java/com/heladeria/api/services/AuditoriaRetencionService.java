package com.heladeria.api.services;

import com.heladeria.api.entities.AuditoriaLog;
import com.heladeria.api.repositories.AuditoriaLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Retención de la bitácora de auditoría: los eventos más viejos que el plazo se guardan primero en un archivo
 * comprimido (un CSV por mes, en el directorio de respaldos) y recién después se borran de la base.
 * Si el archivo no se puede escribir, no se borra nada.
 */
@Service
public class AuditoriaRetencionService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaRetencionService.class);
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int LOTE = 1000;

    private final AuditoriaLogRepository repository;
    private final TransactionTemplate tx;

    @Value("${app.auditoria.retencion-dias:730}")
    private long retencionDias = 730;

    @Value("${app.auditoria.archivo-dir:${app.backup.dir:/app/backups}/auditoria}")
    private String archivoDir;

    public AuditoriaRetencionService(AuditoriaLogRepository repository, org.springframework.transaction.PlatformTransactionManager tm) {
        this.repository = repository;
        this.tx = new TransactionTemplate(tm);
    }

    /** Archiva y borra lo anterior al plazo de retención. Devuelve cuántos eventos se archivaron. */
    public int aplicar() {
        return aplicar(LocalDateTime.now().minusDays(retencionDias));
    }

    public int aplicar(LocalDateTime limite) {
        int total = 0;
        try {
            Files.createDirectories(Path.of(archivoDir));
            while (true) {
                Integer n = tx.execute(st -> {
                    try {
                        return archivarLote(limite);
                    } catch (IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
                if (n == null) {
                    break;
                }
                total += n;
                if (n < LOTE) {
                    break;
                }
            }
        } catch (IOException | java.io.UncheckedIOException e) {
            log.error("No se pudo archivar la auditoría en '{}': {}. No se borró más.", archivoDir, e.getMessage());
        }
        if (total > 0) {
            log.info("Auditoría: {} eventos anteriores a {} archivados en '{}' y retirados de la base.", total, limite, archivoDir);
        }
        return total;
    }

    private int archivarLote(LocalDateTime limite) throws IOException {
        List<AuditoriaLog> lote = repository.findByFechaBeforeOrderByFechaAsc(limite, PageRequest.of(0, LOTE));
        if (lote.isEmpty()) {
            return 0;
        }
        // Un archivo por mes: los eventos del lote se reparten en sus meses
        var porMes = new java.util.TreeMap<String, List<AuditoriaLog>>();
        for (AuditoriaLog a : lote) {
            porMes.computeIfAbsent(a.getFecha().format(MES), k -> new java.util.ArrayList<>()).add(a);
        }
        for (var e : porMes.entrySet()) {
            Path destino = Path.of(archivoDir, "auditoria_" + e.getKey() + ".csv.gz");
            // Cada tanda se agrega como un miembro gzip más (los lectores gzip concatenan miembros)
            try (var out = Files.newOutputStream(destino, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 Writer w = new OutputStreamWriter(new GZIPOutputStream(out), StandardCharsets.UTF_8)) {
                for (AuditoriaLog a : e.getValue()) {
                    w.write(String.join(";", String.valueOf(a.getId()), String.valueOf(a.getFecha()), csv(a.getUsuario()),
                            csv(a.getModulo()), csv(a.getAccion()), String.valueOf(a.getEntidadId()), csv(a.getEntidadNombre()),
                            csv(a.getDetalle()), csv(a.getIpOrigen())) + "\n");
                }
            }
        }
        repository.deleteAllInBatch(lote);
        return lote.size();
    }

    private static String csv(String v) {
        if (v == null) return "";
        return "\"" + v.replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\"";
    }
}
