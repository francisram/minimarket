package com.heladeria.api;

import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.security.JwtService;
import com.heladeria.api.services.SesionAuthService;
import org.springframework.stereotype.Component;

/** Emite tokens de acceso válidos para pruebas: el token debe pertenecer a una sesión vigente del usuario. */
@Component
public class TestTokens {

    private final UsuarioRepository usuarioRepository;
    private final SesionAuthService sesionAuthService;
    private final JwtService jwtService;

    public TestTokens(UsuarioRepository usuarioRepository, SesionAuthService sesionAuthService, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.sesionAuthService = sesionAuthService;
        this.jwtService = jwtService;
    }

    public String emitir(String username, String rol) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("El usuario de prueba '" + username + "' debe existir antes de emitir su token"));
        var sesion = sesionAuthService.crear(usuario, "test");
        return jwtService.generateToken(username, rol, sesion.sesionId());
    }
}
