package com.heladeria.api.concurrency;

import com.heladeria.api.dto.EstadoRespaldoDTO;
import com.heladeria.api.exceptions.ReglaDeNegocioException;
import com.heladeria.api.services.BackupAutomaticoService;
import com.heladeria.api.services.BackupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Ciclo completo de respaldo contra PostgreSQL real: pg_dump, restauración de prueba y copia externa. */
@SpringBootTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RespaldoAutomaticoIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static final Path DIR_RESPALDOS = crearTemporal("respaldos");
    static final Path DIR_EXTERNO = crearTemporal("externo");

    static Path crearTemporal(String prefijo) {
        try {
            return Files.createTempDirectory("it_" + prefijo + "_");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        r.add("app.backup.dir", DIR_RESPALDOS::toString);
        r.add("app.backup.copia-externa-dir", DIR_EXTERNO::toString);
    }

    @Autowired BackupAutomaticoService automatico;
    @Autowired BackupService backupService;

    @Test
    @Order(1)
    @DisplayName("Sin respaldos, el estado exige atención")
    void sinRespaldosHayAlerta() {
        EstadoRespaldoDTO estado = automatico.obtenerEstado();
        assertTrue(estado.requiereAtencion());
        assertTrue(estado.alertas().stream().anyMatch(a -> a.contains("No existe ningún respaldo")));
        assertTrue(automatico.respaldoVencido());
    }

    @Test
    @Order(2)
    @DisplayName("El ciclo crea el dump, lo verifica restaurándolo en una base temporal y lo copia fuera del servidor")
    void cicloCompleto() {
        assertTrue(automatico.ejecutarCiclo());

        EstadoRespaldoDTO e = automatico.obtenerEstado();
        assertEquals("OK", e.resultadoProgramado(), e.detalleProgramado());
        assertEquals("OK", e.resultadoVerificacion(), e.detalleVerificacion());
        assertEquals("COMPLETA", e.nivelVerificacion());
        assertEquals("OK", e.resultadoCopiaExterna(), e.detalleCopiaExterna());
        assertFalse(e.requiereAtencion(), String.valueOf(e.alertas()));
        assertNotNull(e.ultimoRespaldo());

        File[] enExterno = DIR_EXTERNO.toFile().listFiles((d, n) -> n.endsWith(".dump"));
        assertNotNull(enExterno);
        assertEquals(1, enExterno.length);
        assertEquals(e.ultimoRespaldo().getTamanoBytes(), enExterno[0].length(), "La copia externa es idéntica en tamaño");
        assertFalse(automatico.respaldoVencido());
    }

    @Test
    @Order(3)
    @DisplayName("La base temporal de verificación no queda olvidada")
    void noQuedanBasesTemporales() throws Exception {
        try (var c = java.sql.DriverManager.getConnection(
                postgres.getJdbcUrl().replace("/" + postgres.getDatabaseName(), "/postgres"), postgres.getUsername(), postgres.getPassword());
             var rs = c.createStatement().executeQuery("select count(*) from pg_database where datname like 'verif_restore_%'")) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }
    }

    @Test
    @Order(4)
    @DisplayName("Un archivo corrupto NO pasa la verificación y el estado lo señala")
    void respaldoCorruptoFallaVerificacion() throws Exception {
        File corrupto = new File(backupService.getBackupDir(), "backup_heladeria_corrupto.dump");
        Files.writeString(corrupto.toPath(), "esto no es un dump de postgres");

        assertThrows(ReglaDeNegocioException.class, () -> backupService.verificarRespaldo(corrupto.getName()));
        corrupto.delete();
    }

    @Test
    @Order(5)
    @DisplayName("Si la copia externa no se puede escribir, el ciclo lo registra como alerta")
    void falloDeCopiaExternaGeneraAlerta() throws Exception {
        // Sustituimos el directorio externo por un archivo regular: no se puede crear ahí
        Path externo = DIR_EXTERNO;
        File[] contenido = externo.toFile().listFiles();
        if (contenido != null) for (File f : contenido) f.delete();
        Files.delete(externo);
        Files.writeString(externo, "no soy un directorio");
        try {
            automatico.ejecutarCiclo();
            EstadoRespaldoDTO e = automatico.obtenerEstado();
            assertEquals("OK", e.resultadoProgramado());
            assertEquals("ERROR", e.resultadoCopiaExterna());
            assertTrue(e.requiereAtencion());
            assertTrue(e.alertas().stream().anyMatch(a -> a.contains("copia del respaldo fuera del servidor")));
        } finally {
            Files.delete(externo);
            Files.createDirectory(externo);
        }
    }
}
