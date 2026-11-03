package com.heladeria.api.services;

import com.heladeria.api.dto.CambiarPasswordDTO;
import com.heladeria.api.dto.LoginRequestDTO;
import com.heladeria.api.dto.LoginResponseDTO;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.exceptions.PasswordExpiradaException;
import com.heladeria.api.repositories.UsuarioRepository;
import com.heladeria.api.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SesionAuthService sesionAuthService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       SesionAuthService sesionAuthService) {
        this.sesionAuthService = sesionAuthService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto) {
        return login(dto, null);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO dto, String dispositivo) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        verificarVigenciaPassword(usuario);

        return emitirToken(sesionAuthService.crear(usuario, dispositivo));
    }

    @Transactional
    public LoginResponseDTO cambiarPassword(CambiarPasswordDTO dto) {
        return cambiarPassword(dto, null);
    }

    @Transactional
    public LoginResponseDTO cambiarPassword(CambiarPasswordDTO dto, String dispositivo) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        if (dto.getPasswordNueva().equals(dto.getPasswordActual())) {
            throw new com.heladeria.api.exceptions.ReglaDeNegocioException("La nueva contraseña debe ser distinta de la actual.");
        }
        if (com.heladeria.api.config.ClavesPorDefectoCheck.CLAVES_CONOCIDAS.contains(dto.getPasswordNueva())) {
            throw new com.heladeria.api.exceptions.ReglaDeNegocioException("Esa contraseña es de uso común o de demostración. Elija una propia.");
        }

        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);

        // Cambiar la contraseña cierra todas las sesiones abiertas (p. ej. un token copiado deja de servir)
        sesionAuthService.revocarTodas(usuario.getId());
        return emitirToken(sesionAuthService.crear(usuario, dispositivo));
    }

    private void verificarVigenciaPassword(Usuario usuario) {
        if (Boolean.TRUE.equals(usuario.getDebeCambiarPassword())) {
            throw new PasswordExpiradaException("DEBE_CAMBIAR_PASSWORD", "Debe cambiar su contraseña antes de continuar");
        }
        if (!Boolean.TRUE.equals(usuario.getPasswordNuncaExpira())
                && usuario.getPasswordExpiraEl() != null
                && usuario.getPasswordExpiraEl().isBefore(LocalDate.now())) {
            throw new PasswordExpiradaException("PASSWORD_EXPIRADA", "Su contraseña ha expirado");
        }
    }

    /** Renueva el token de acceso con el de refresco (que se rota). */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public LoginResponseDTO refrescar(String refreshToken) {
        return emitirToken(sesionAuthService.renovar(refreshToken));
    }

    @Transactional
    public void cerrarSesion(String refreshToken) {
        sesionAuthService.revocarPorRefresh(refreshToken);
    }

    /** Cierra todas las sesiones de un usuario (acción de administrador). */
    @Transactional
    public int cerrarSesionesDe(String username) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new com.heladeria.api.exceptions.RecursoNoEncontradoException("Usuario no encontrado: " + username));
        return sesionAuthService.revocarTodas(u.getId());
    }

    private LoginResponseDTO emitirToken(SesionAuthService.SesionEmitida sesion) {
        Usuario usuario = sesion.usuario();
        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombreRol() : null;
        String token = jwtService.generateToken(usuario.getUsername(), rolNombre, sesion.sesionId());
        return new LoginResponseDTO(token, sesion.refreshToken(), usuario.getUsername(), rolNombre);
    }
}
