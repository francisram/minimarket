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
