package com.heladeria.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    /** Prefijo del secreto de demostración que venía en el código fuente (público: no sirve para producción). */
    static final String PREFIJO_SECRETO_POR_DEFECTO = "dGhpcy1pcy1hLXZlcnktc2VjdXJl";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret:dGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXktZm9yLWhlbGFkZXJpYS1hcGktMjU2LWJpdHM=}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs,
            @Value("${spring.datasource.url:}") String datasourceUrl) {
        // Con PostgreSQL (producción) un secreto débil o por defecto impide arrancar; con H2 (desarrollo/tests) solo advierte.
        validarSecreto(secret, datasourceUrl != null && datasourceUrl.startsWith("jdbc:postgresql"));
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    static void validarSecreto(String secret, boolean estricto) {
        boolean debil = secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32
                || secret.startsWith(PREFIJO_SECRETO_POR_DEFECTO);
        if (!debil) {
            return;
        }
        String mensaje = "SEGURIDAD: el secreto JWT (APP_JWT_SECRET) es el de demostración del código fuente o tiene menos de 32 bytes. "
                + "Cualquiera podría fabricar tokens válidos. Defina un secreto aleatorio, por ejemplo: openssl rand -base64 48";
        if (estricto) {
            throw new IllegalStateException(mensaje);
        }
        org.slf4j.LoggerFactory.getLogger(JwtService.class).warn(mensaje + " (modo desarrollo: solo advertencia)");
    }

    public String generateToken(String username, String rol) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRol(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return parseClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
