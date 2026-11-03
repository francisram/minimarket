package com.heladeria.api.services;

import com.heladeria.api.dto.BackupInfoDTO;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    @Value("${app.backup.dir:/app/backups}")
    private String backupDirPath;

    /** Directorio de copia fuera del servidor (NAS, disco USB, montaje de red). Vacío = sin copia externa. */
    @Value("${app.backup.copia-externa-dir:}")
    private String copiaExternaDirPath;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:postgres}")
    private String dbUser;

    @Value("${spring.datasource.password:postgres}")
    private String dbPass;

    private File backupDir;

    @PostConstruct
    public void init() {
        backupDir = new File(backupDirPath);
        if (!backupDir.exists()) {
            boolean created = backupDir.mkdirs();
            if (!created) {
                String fallbackPath = System.getProperty("user.home") + File.separator + ".minimarket" + File.separator + "backups";
                log.warn("No se pudo crear '{}'. Usando directorio de respaldo alternativo: '{}'", backupDirPath, fallbackPath);
                backupDir = new File(fallbackPath);
                backupDir.mkdirs();
            }
        }
        log.info("Directorio de respaldos inicializado en: {}", backupDir.getAbsolutePath());
    }

    public BackupInfoDTO crearRespaldo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "backup_heladeria_" + timestamp + ".dump";
        File archivoDestino = new File(backupDir, nombreArchivo);

        DbParams params = parseDbParams();

        log.info("Iniciando pg_dump para la base de datos '{}' en '{}:{}'...", params.database, params.host, params.port);

        List<String> comando = List.of(
                "pg_dump",
                "-h", params.host,
                "-p", String.valueOf(params.port),
                "-U", params.user,
                "-d", params.database,
                "-Fc",
                "-f", archivoDestino.getAbsolutePath()
        );

        ejecutarProceso(comando, params.password, "pg_dump", 120);

        if (!archivoDestino.exists() || archivoDestino.length() == 0) {
            if (archivoDestino.exists()) {
                archivoDestino.delete();
            }
            throw new ReglaDeNegocioException("El archivo de respaldo no se generó correctamente o está vacío.");
        }

        log.info("Respaldo '{}' generado exitosamente con tamaño {} bytes.", nombreArchivo, archivoDestino.length());
        return mapToDTO(archivoDestino);
    }

    public List<BackupInfoDTO> listarRespaldos() {
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] archivos = backupDir.listFiles((dir, name) -> name.endsWith(".dump") || name.endsWith(".sql"));
        if (archivos == null || archivos.length == 0) {
            return Collections.emptyList();
        }

        List<File> listaArchivos = new ArrayList<>(Arrays.asList(archivos));
        listaArchivos.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        List<BackupInfoDTO> dtos = new ArrayList<>();
        for (File f : listaArchivos) {
            dtos.add(mapToDTO(f));
        }
        return dtos;
    }

    public Resource obtenerArchivo(String nombreArchivo) {
        validarNombreArchivo(nombreArchivo);
        File archivo = new File(backupDir, nombreArchivo);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new RecursoNoEncontradoException("El archivo de respaldo '" + nombreArchivo + "' no existe.");
        }
        return new FileSystemResource(archivo);
    }

    public void eliminarRespaldo(String nombreArchivo) {
        validarNombreArchivo(nombreArchivo);
        File archivo = new File(backupDir, nombreArchivo);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new RecursoNoEncontradoException("El archivo de respaldo '" + nombreArchivo + "' no existe.");
        }
        boolean deleted = archivo.delete();
        if (!deleted) {
            throw new ReglaDeNegocioException("No se pudo eliminar el archivo de respaldo '" + nombreArchivo + "'.");
        }
        log.info("Archivo de respaldo eliminado: {}", nombreArchivo);
    }

    public void restaurarDesdeArchivo(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ReglaDeNegocioException("Debe seleccionar un archivo para restaurar.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".dump") && !originalFilename.endsWith(".sql"))) {
            throw new ReglaDeNegocioException("Formato no válido. Debe ser un archivo .dump o .sql");
        }

        String tempName = "temp_restore_" + System.currentTimeMillis() + ".dump";
        File tempFile = new File(backupDir, tempName);

        try {
            Files.copy(file.getInputStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ejecutarRestauracion(tempFile);
        } catch (Exception e) {
            log.error("Error al restaurar desde archivo subido: ", e);
            throw new ReglaDeNegocioException("Fallo en la restauración: " + e.getMessage());
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public void restaurarDesdeServidor(String nombreArchivo) {
        validarNombreArchivo(nombreArchivo);
        File archivo = new File(backupDir, nombreArchivo);
        if (!archivo.exists() || !archivo.isFile()) {
            throw new RecursoNoEncontradoException("El archivo de respaldo '" + nombreArchivo + "' no existe en el servidor.");
        }
        ejecutarRestauracion(archivo);
    }

    private void ejecutarRestauracion(File archivoDump) {
        DbParams params = parseDbParams();
        log.warn("¡ADVERTENCIA! Iniciando restauración de base de datos desde '{}' sobre '{}'...", archivoDump.getName(), params.database);

        List<String> comando = List.of(
                "pg_restore",
                "-h", params.host,
                "-p", String.valueOf(params.port),
                "-U", params.user,
                "-d", params.database,
                "--clean",
                "--if-exists",
                "--no-owner",
                archivoDump.getAbsolutePath()
        );

        ejecutarProcesoRestauracion(comando, params.password, 180);
        log.info("Restauración de base de datos finalizada exitosamente desde '{}'.", archivoDump.getName());
    }

    private void ejecutarProceso(List<String> comando, String password, String operacion, int timeoutSegundos) {
        ProcessBuilder pb = new ProcessBuilder(comando);
        if (password != null && !password.isBlank()) {
            pb.environment().put("PGPASSWORD", password);
        }

        try {
            Process process = pb.start();
            StringBuilder errorOutput = new StringBuilder();

            Thread errorReader = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });

            boolean finished = process.waitFor(timeoutSegundos, TimeUnit.SECONDS);
            errorReader.join(2000);

            if (!finished) {
                process.destroyForcibly();
                throw new ReglaDeNegocioException("La operación de " + operacion + " superó el tiempo límite de espera (" + timeoutSegundos + "s).");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Error en proceso {}. Exit code: {}. Detalle: {}", operacion, exitCode, errorOutput);
                throw new ReglaDeNegocioException("Error ejecutando " + operacion + ": " + errorOutput.toString().trim());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaDeNegocioException("Proceso de " + operacion + " interrumpido.");
        } catch (Exception e) {
            if (e instanceof ReglaDeNegocioException) throw (ReglaDeNegocioException) e;
            log.error("Fallo inesperado al ejecutar {}: ", operacion, e);
            throw new ReglaDeNegocioException("Error en " + operacion + ": " + e.getMessage());
        }
    }

    private void ejecutarProcesoRestauracion(List<String> comando, String password, int timeoutSegundos) {
        ProcessBuilder pb = new ProcessBuilder(comando);
        if (password != null && !password.isBlank()) {
            pb.environment().put("PGPASSWORD", password);
        }

        try {
            Process process = pb.start();
            StringBuilder errorOutput = new StringBuilder();

            Thread errorReader = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });

            boolean finished = process.waitFor(timeoutSegundos, TimeUnit.SECONDS);
            errorReader.join(2000);

            if (!finished) {
                process.destroyForcibly();
                throw new ReglaDeNegocioException("La restauración superó el tiempo límite de espera.");
            }

            int exitCode = process.exitValue();
            if (exitCode > 1) {
                log.error("Error fatal en pg_restore. Exit code: {}. Detalle: {}", exitCode, errorOutput);
                throw new ReglaDeNegocioException("Error al restaurar la base de datos: " + errorOutput.toString().trim());
            } else if (exitCode == 1) {
                log.warn("pg_restore completado con advertencias (exit code 1): {}", errorOutput);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaDeNegocioException("Proceso de restauración interrumpido.");
        } catch (Exception e) {
            if (e instanceof ReglaDeNegocioException) throw (ReglaDeNegocioException) e;
            log.error("Fallo inesperado al restaurar: ", e);
            throw new ReglaDeNegocioException("Error al restaurar: " + e.getMessage());
        }
    }


    // ------------------------------------------------------------------------------------------
    // Respaldos automáticos: copia externa y verificación
    // ------------------------------------------------------------------------------------------

    public record ResultadoVerificacion(String nivel, String detalle) { }

    public boolean copiaExternaConfigurada() {
        return copiaExternaDirPath != null && !copiaExternaDirPath.isBlank();
    }

    public File getBackupDir() {
        return backupDir;
    }

    /** Copia el respaldo al directorio externo (archivo temporal + renombrado) y comprueba que el tamaño coincida. */
    public void copiarACopiaExterna(String nombreArchivo) {
        validarNombreArchivo(nombreArchivo);
        if (!copiaExternaConfigurada()) {
            throw new ReglaDeNegocioException("No hay un directorio de copia externa configurado.");
        }
        File origen = new File(backupDir, nombreArchivo);
        File destinoDir = new File(copiaExternaDirPath);
        try {
            Files.createDirectories(destinoDir.toPath());
            File temporal = new File(destinoDir, nombreArchivo + ".part");
            File destino = new File(destinoDir, nombreArchivo);
            Files.copy(origen.toPath(), temporal.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (temporal.length() != origen.length()) {
                temporal.delete();
                throw new ReglaDeNegocioException("La copia externa quedó incompleta (" + temporal.length() + " de " + origen.length() + " bytes).");
            }
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.info("Respaldo '{}' copiado a '{}'.", nombreArchivo, destinoDir.getAbsolutePath());
        } catch (ReglaDeNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new ReglaDeNegocioException("No se pudo copiar el respaldo a '" + copiaExternaDirPath + "': " + e.getMessage());
        }
    }

    /**
     * Verifica que el respaldo realmente sirva: primero su integridad (pg_restore --list) y luego,
     * si el usuario de la base puede crear bases, lo restaura en una base temporal y compara las tablas con la real.
     */
    public ResultadoVerificacion verificarRespaldo(String nombreArchivo) {
        validarNombreArchivo(nombreArchivo);
        File archivo = new File(backupDir, nombreArchivo);
        if (!archivo.exists() || !archivo.isFile() || archivo.length() == 0) {
            throw new ReglaDeNegocioException("El respaldo '" + nombreArchivo + "' no existe o está vacío.");
        }
        DbParams p = parseDbParams();

        // 1) Integridad del archivo: lee el índice del dump sin tocar ninguna base
        ejecutarConSalida(List.of("pg_restore", "--list", archivo.getAbsolutePath()), p.password, 60);

        // 2) Restauración real en una base temporal
        String temporal = "verif_restore_" + System.currentTimeMillis();
        boolean creada = false;
        try {
            try {
                ejecutarConSalida(psql(p, "postgres", "CREATE DATABASE " + temporal), p.password, 60);
                creada = true;
            } catch (ReglaDeNegocioException e) {
                log.warn("No se pudo crear la base temporal de verificación ({}). Solo se verificó la integridad del archivo.", e.getMessage());
                return new ResultadoVerificacion("INTEGRIDAD", "Archivo íntegro. No se pudo restaurar en una base temporal: " + e.getMessage());
            }

            ProcessBuilder pb = new ProcessBuilder("pg_restore", "-h", p.host, "-p", String.valueOf(p.port), "-U", p.user,
                    "-d", temporal, "--no-owner", archivo.getAbsolutePath());
            ejecutarProcesoRestauracionVerificacion(pb, p.password);

            String tablasRespaldo = ejecutarConSalida(psql(p, temporal, SQL_CONTAR_TABLAS), p.password, 60).trim();
            String tablasReales = ejecutarConSalida(psql(p, p.database, SQL_CONTAR_TABLAS), p.password, 60).trim();
            if (tablasRespaldo.isEmpty() || "0".equals(tablasRespaldo)) {
                throw new ReglaDeNegocioException("El respaldo se restauró pero no contiene tablas.");
            }
            if (!tablasRespaldo.equals(tablasReales)) {
                throw new ReglaDeNegocioException("El respaldo restaurado tiene " + tablasRespaldo + " tablas y la base actual " + tablasReales + ".");
            }
            return new ResultadoVerificacion("COMPLETA", "Restauración de prueba correcta (" + tablasRespaldo + " tablas).");
        } finally {
            if (creada) {
                try {
                    ejecutarConSalida(psql(p, "postgres", "DROP DATABASE IF EXISTS " + temporal), p.password, 60);
                } catch (Exception e) {
                    log.error("No se pudo eliminar la base temporal '{}': {}", temporal, e.getMessage());
                }
            }
        }
    }

    private static final String SQL_CONTAR_TABLAS =
            "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'";

    private List<String> psql(DbParams p, String database, String sql) {
        return List.of("psql", "-h", p.host, "-p", String.valueOf(p.port), "-U", p.user, "-d", database,
                "-v", "ON_ERROR_STOP=1", "-tA", "-c", sql);
    }

    private void ejecutarProcesoRestauracionVerificacion(ProcessBuilder pb, String password) {
        if (password != null && !password.isBlank()) {
            pb.environment().put("PGPASSWORD", password);
        }
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            byte[][] salida = new byte[1][];
            Thread lector = Thread.ofVirtual().start(() -> {
                try { salida[0] = process.getInputStream().readAllBytes(); } catch (Exception ignored) { }
            });
            boolean terminado = process.waitFor(300, TimeUnit.SECONDS);
            lector.join(2000);
            if (!terminado) {
                process.destroyForcibly();
                throw new ReglaDeNegocioException("La restauración de prueba superó el tiempo límite.");
            }
            // pg_restore devuelve 1 con advertencias no fatales; más de 1 es un fallo real
            if (process.exitValue() > 1) {
                String detalle = salida[0] != null ? new String(salida[0]).trim() : "";
                throw new ReglaDeNegocioException("La restauración de prueba falló: " + detalle);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaDeNegocioException("Verificación interrumpida.");
        } catch (ReglaDeNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new ReglaDeNegocioException("Error en la restauración de prueba: " + e.getMessage());
        }
    }

    /** Ejecuta un comando y devuelve su salida estándar; si falla lanza el error. */
    private String ejecutarConSalida(List<String> comando, String password, int timeoutSegundos) {
        ProcessBuilder pb = new ProcessBuilder(comando);
        if (password != null && !password.isBlank()) {
            pb.environment().put("PGPASSWORD", password);
        }
        try {
            Process process = pb.start();
            byte[][] out = new byte[1][];
            byte[][] err = new byte[1][];
            Thread t1 = Thread.ofVirtual().start(() -> { try { out[0] = process.getInputStream().readAllBytes(); } catch (Exception ignored) { } });
            Thread t2 = Thread.ofVirtual().start(() -> { try { err[0] = process.getErrorStream().readAllBytes(); } catch (Exception ignored) { } });
            boolean terminado = process.waitFor(timeoutSegundos, TimeUnit.SECONDS);
            t1.join(2000);
            t2.join(2000);
            if (!terminado) {
                process.destroyForcibly();
                throw new ReglaDeNegocioException("'" + comando.get(0) + "' superó el tiempo límite (" + timeoutSegundos + "s).");
            }
            if (process.exitValue() != 0) {
                String detalle = err[0] != null ? new String(err[0]).trim() : "";
                throw new ReglaDeNegocioException("'" + comando.get(0) + "' falló: " + detalle);
            }
            return out[0] != null ? new String(out[0]) : "";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ReglaDeNegocioException("Proceso interrumpido.");
        } catch (ReglaDeNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new ReglaDeNegocioException("Error al ejecutar '" + comando.get(0) + "': " + e.getMessage());
        }
    }

    private void validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new ReglaDeNegocioException("El nombre del archivo no puede estar vacío");
        }
        if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
            throw new ReglaDeNegocioException("Nombre de archivo inválido.");
        }
        if (!nombreArchivo.endsWith(".dump") && !nombreArchivo.endsWith(".sql")) {
            throw new ReglaDeNegocioException("Extensión de archivo no permitida. Solo se admiten archivos .dump y .sql");
        }
    }

    private BackupInfoDTO mapToDTO(File file) {
        long bytes = file.length();
        LocalDateTime fecha = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        return new BackupInfoDTO(file.getName(), bytes, formatTamano(bytes), fecha);
    }

    private String formatTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.US, "%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    private DbParams parseDbParams() {
        String host = "db";
        int port = 5432;
        String database = "heladeriadb";

        if (dbUrl != null && dbUrl.startsWith("jdbc:postgresql://")) {
            Pattern pattern = Pattern.compile("jdbc:postgresql://([^:/]+)(?::(\\d+))?/([^?]+)");
            Matcher matcher = pattern.matcher(dbUrl);
            if (matcher.find()) {
                host = matcher.group(1);
                if (matcher.group(2) != null) {
                    port = Integer.parseInt(matcher.group(2));
                }
                database = matcher.group(3);
            }
        }

        return new DbParams(host, port, database, dbUser, dbPass);
    }

    private record DbParams(String host, int port, String database, String user, String password) {}
}
