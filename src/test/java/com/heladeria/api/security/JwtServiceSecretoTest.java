package com.heladeria.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Validación del secreto JWT")
class JwtServiceSecretoTest {

    private static final String SECRETO_POR_DEFECTO =
            "dGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXktZm9yLWhlbGFkZXJpYS1hcGktMjU2LWJpdHM=";
    private static final String SECRETO_FUERTE = "q8F2xk0Lr9mZp3TvN7yB1cHs5WdE6gJaUoX4iKtRnMb2VfYe8ZlQwP0uSh3DjCaA";

    @Test
    @DisplayName("En producción (PostgreSQL) el secreto por defecto impide arrancar")
    void secretoPorDefectoFallaEnProduccion() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new JwtService(SECRETO_POR_DEFECTO, 3_600_000, "jdbc:postgresql://db:5432/heladeriadb"));
        assertTrue(e.getMessage().contains("APP_JWT_SECRET"));
    }

    @Test
    @DisplayName("En producción un secreto corto (menos de 32 bytes) también se rechaza")
    void secretoCortoFallaEnProduccion() {
        assertThrows(IllegalStateException.class, () -> JwtService.validarSecreto("corto", true));
    }

    @Test
    @DisplayName("En producción un secreto aleatorio largo es aceptado y firma tokens válidos")
    void secretoFuerteFuncionaEnProduccion() {
        JwtService jwt = new JwtService(SECRETO_FUERTE, 3_600_000, "jdbc:postgresql://db:5432/heladeriadb");
        String token = jwt.generateToken("cajero", "CAJERO");
        assertTrue(jwt.isTokenValid(token));
        assertEquals("cajero", jwt.extractUsername(token));
    }

    @Test
    @DisplayName("En desarrollo (H2) el secreto por defecto solo advierte, no impide arrancar")
    void secretoPorDefectoSoloAdvierteEnDesarrollo() {
        assertDoesNotThrow(() -> new JwtService(SECRETO_POR_DEFECTO, 3_600_000, "jdbc:h2:mem:heladeriadb"));
    }

    @Test
    @DisplayName("Un token firmado con otro secreto no es válido")
    void tokenDeOtroSecretoNoValida() {
        JwtService a = new JwtService(SECRETO_FUERTE, 3_600_000, "jdbc:postgresql://x/y");
        JwtService b = new JwtService(SECRETO_FUERTE.replace('q', 'z'), 3_600_000, "jdbc:postgresql://x/y");
        assertFalse(b.isTokenValid(a.generateToken("admin", "ADMIN")));
    }
}
