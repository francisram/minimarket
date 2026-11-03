package com.heladeria.api.services;

import com.heladeria.api.entities.SesionAuth;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.repositories.SesionAuthRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Sesiones revocables con token de refresco rotativo.
 * El token de refresco se entrega una sola vez y en la base queda solo su hash SHA-256.
 */
@Service
public class SesionAuthService {

    /** Si el token anterior se reusa dentro de este margen se trata como una carrera entre pestañas, no como robo. */
    static final Duration MARGEN_REUSO = Duration.ofSeconds(10);

    private final SesionAuthRepository repository;
    private final long refreshMs;
    private final SecureRandom random = new SecureRandom();

    public SesionAuthService(SesionAuthRepository repository,
                             @Value("${app.jwt.refresh-expiration-ms:43200000}") long refreshMs) {
        this.repository = repository;
        this.refreshMs = refreshMs;
    }

    public record SesionEmitida(String sesionId, String refreshToken, Usuario usuario) { }

    @Transactional
    public SesionEmitida crear(Usuario usuario, String dispositivo) {
        LocalDateTime ahora = LocalDateTime.now();
        repository.borrarExpiradasAntesDe(ahora.minusDays(1));
        String refresh = nuevoToken();
        SesionAuth s = new SesionAuth();
        s.setId(UUID.randomUUID().toString());
        s.setUsuario(usuario);
        s.setRefreshHash(hash(refresh));
        s.setCreadaEn(ahora);
        s.setExpiraEn(ahora.plus(Duration.ofMillis(refreshMs)));
        s.setDispositivo(dispositivo != null && dispositivo.length() > 200 ? dispositivo.substring(0, 200) : dispositivo);
        repository.save(s);
        return new SesionEmitida(s.getId(), refresh, usuario);
    }

    /** Valida y rota el token de refresco. Un token ya usado fuera del margen revoca toda la sesión. */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public SesionEmitida renovar(String refreshToken) {
        LocalDateTime ahora = LocalDateTime.now();
        String h = hash(refreshToken);
        SesionAuth s = repository.findByRefreshHashActualOAnterior(h)
                .orElseThrow(() -> new BadCredentialsException("Sesión inválida"));

        if (!s.estaActiva(ahora)) {
            throw new BadCredentialsException("Sesión expirada");
        }
        if (!h.equals(s.getRefreshHash())) {
            // Llegó el token anterior: o fue una carrera entre pestañas o alguien lo copió
            boolean carrera = s.getRotadoEn() != null && s.getRotadoEn().plus(MARGEN_REUSO).isAfter(ahora);
            if (!carrera) {
                s.setRevocadaEn(ahora);
                repository.save(s);
            }
            throw new BadCredentialsException("Sesión inválida");
        }
        Usuario u = s.getUsuario();
        if (!Boolean.TRUE.equals(u.getEstado())) {
            s.setRevocadaEn(ahora);
            repository.save(s);
            throw new BadCredentialsException("Sesión inválida");
        }

        String nuevo = nuevoToken();
        s.setRefreshHashAnterior(s.getRefreshHash());
        s.setRefreshHash(hash(nuevo));
        s.setRotadoEn(ahora);
        s.setExpiraEn(ahora.plus(Duration.ofMillis(refreshMs)));
        repository.save(s);
        return new SesionEmitida(s.getId(), nuevo, u);
    }

    /** Cierra la sesión a la que pertenece el token (logout). Idempotente. */
    @Transactional
    public void revocarPorRefresh(String refreshToken) {
        repository.findByRefreshHashActualOAnterior(hash(refreshToken)).ifPresent(s -> {
            if (s.getRevocadaEn() == null) {
                s.setRevocadaEn(LocalDateTime.now());
                repository.save(s);
            }
        });
    }

    @Transactional
    public int revocarTodas(Long usuarioId) {
        return repository.revocarTodasDelUsuario(usuarioId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public boolean estaActiva(String sesionId) {
        return sesionId != null && repository.findById(sesionId)
                .map(s -> s.estaActiva(LocalDateTime.now())).orElse(false);
    }

    private String nuevoToken() {
        byte[] b = new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
