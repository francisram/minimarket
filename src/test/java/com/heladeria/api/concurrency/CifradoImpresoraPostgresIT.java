package com.heladeria.api.concurrency;

import com.heladeria.api.entities.Impresora;
import com.heladeria.api.entities.TipoConexion;
import com.heladeria.api.repositories.ImpresoraRepository;
import com.heladeria.api.security.CifradoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/** La contraseña de impresoras se guarda cifrada en PostgreSQL real (migración V6 + converter, con Hibernate en modo validate). */
@SpringBootTest
@Testcontainers
class CifradoImpresoraPostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        r.add("app.jwt.secret", () -> "q8F2xk0Lr9mZp3TvN7yB1cHs5WdE6gJaUoX4iKtRnMb2VfYe8ZlQwP0uSh3DjCaA");
        r.add("app.seguridad.clave-cifrado", () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired ImpresoraRepository repository;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("Una contraseña larga se guarda cifrada (cabe en la columna ampliada) y se recupera en claro")
    void guardaCifradoEnPostgres() {
        String larga = "ContraseñaMuyLarga-" + "x".repeat(60);
        Impresora i = new Impresora();
        i.setNombre("Windows PG");
        i.setTipoConexion(TipoConexion.WINDOWS);
        i.setIp("10.0.0.5");
        i.setPuerto(445);
        i.setCola("TICKETS");
        i.setUsuario("caja");
        i.setPassword(larga);
        Impresora guardada = repository.saveAndFlush(i);

        String cruda = jdbc.queryForObject("SELECT password FROM impresoras WHERE id = ?", String.class, guardada.getId());
        assertTrue(cruda.startsWith(CifradoService.PREFIJO));
        assertFalse(cruda.contains("ContraseñaMuyLarga"));
        assertTrue(cruda.length() > 100, "El valor cifrado es más largo que la columna original de 100");
        assertEquals(larga, repository.findById(guardada.getId()).orElseThrow().getPassword());
    }
}
