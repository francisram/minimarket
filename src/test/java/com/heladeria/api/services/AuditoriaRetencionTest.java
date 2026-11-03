package com.heladeria.api.services;

import com.heladeria.api.entities.AuditoriaLog;
import com.heladeria.api.repositories.AuditoriaLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Retención de la auditoría")
class AuditoriaRetencionTest {

    @Autowired private AuditoriaRetencionService servicio;
    @Autowired private AuditoriaLogRepository repository;

    @AfterEach
    void limpiar() {
        repository.deleteAll();
    }

    private java.util.List<AuditoriaLog> propios() {
        return repository.findAll().stream().filter(a -> "PRUEBA".equals(a.getModulo())).toList();
    }

    private AuditoriaLog evento(String detalle, LocalDateTime fecha) {
        AuditoriaLog a = new AuditoriaLog("usuario", "PRUEBA", "ACCION", 1L, "entidad", detalle, "127.0.0.1");
        a.setFecha(fecha);
        return repository.save(a);
    }

    @Test
    @DisplayName("Lo viejo se archiva en un CSV comprimido y se borra; lo reciente se queda")
    void archivaYBorra(@TempDir Path dir) throws Exception {
        ReflectionTestUtils.setField(servicio, "archivoDir", dir.toString());
        evento("evento viejo; con \"comillas\"", LocalDateTime.now().minusDays(800));
        evento("evento reciente", LocalDateTime.now().minusDays(10));

        int archivados = servicio.aplicar(LocalDateTime.now().minusDays(730));

        assertEquals(1, archivados);
        var propios = propios();
        assertEquals(1, propios.size());
        assertEquals("evento reciente", propios.get(0).getDetalle());

        Path archivo = Files.list(dir).filter(p -> p.getFileName().toString().endsWith(".csv.gz")).findFirst().orElseThrow();
        try (var in = new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(archivo)), StandardCharsets.UTF_8))) {
            String linea = in.readLine();
            assertTrue(linea.contains("evento viejo"), linea);
            assertTrue(linea.contains("\"\"comillas\"\""), "las comillas se escapan en el CSV: " + linea);
        }
    }

    @Test
    @DisplayName("Si no se puede escribir el archivo, no se borra nada")
    void sinArchivoNoBorra(@TempDir Path dir) throws Exception {
        Path ocupado = dir.resolve("es-un-archivo");
        Files.writeString(ocupado, "x");
        ReflectionTestUtils.setField(servicio, "archivoDir", ocupado.toString()); // un archivo, no un directorio
        evento("viejo", LocalDateTime.now().minusDays(900));

        servicio.aplicar(LocalDateTime.now().minusDays(730));

        assertEquals(1, propios().size(), "sin poder archivar, el evento se conserva");
    }
}
