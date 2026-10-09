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

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        verificarVigenciaPassword(usuario);

        return emitirToken(usuario);
    }

    @Transactional
    public LoginResponseDTO cambiarPassword(CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .filter(u -> Boolean.TRUE.equals(u.getEstado()))
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        usuario.setPasswordHash(passwordEncoder.encode(dto.getPasswordNueva()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);

        return emitirToken(usuario);
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

    private LoginResponseDTO emitirToken(Usuario usuario) {
        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombreRol() : null;
        String token = jwtService.generateToken(usuario.getUsername(), rolNombre);
        return new LoginResponseDTO(token, usuario.getUsername(), rolNombre);
    }
}
