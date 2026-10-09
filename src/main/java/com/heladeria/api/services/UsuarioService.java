package com.heladeria.api.services;

import com.heladeria.api.dto.UsuarioDTO;
import com.heladeria.api.entities.Rol;
import com.heladeria.api.entities.Usuario;
import com.heladeria.api.exceptions.RecursoNoEncontradoException;
import com.heladeria.api.repositories.RolRepository;
import com.heladeria.api.repositories.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioDTO obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
    }

    @Transactional
    public UsuarioDTO guardar(UsuarioDTO dto) {
        if (usuarioRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + dto.getUsername());
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es requerida para crear un nuevo usuario");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        usuario.setPasswordNuncaExpira(dto.getPasswordNuncaExpira() != null ? dto.getPasswordNuncaExpira() : false);
        usuario.setDebeCambiarPassword(dto.getDebeCambiarPassword() != null ? dto.getDebeCambiarPassword() : false);
        usuario.setPasswordExpiraEl(dto.getPasswordExpiraEl());

        if (dto.getRolId() != null) {
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + dto.getRolId()));
            usuario.setRol(rol);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioDTO actualizar(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));

        if (!usuario.getUsername().equalsIgnoreCase(dto.getUsername()) &&
                usuarioRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Ya existe un usuario con el nombre: " + dto.getUsername());
        }

        usuario.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }
        if (dto.getPasswordNuncaExpira() != null) {
            usuario.setPasswordNuncaExpira(dto.getPasswordNuncaExpira());
        }
        if (dto.getDebeCambiarPassword() != null) {
            usuario.setDebeCambiarPassword(dto.getDebeCambiarPassword());
        }
        usuario.setPasswordExpiraEl(dto.getPasswordExpiraEl());

        if (dto.getRolId() != null) {
            Rol rol = rolRepository.findById(dto.getRolId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado con ID: " + dto.getRolId()));
            usuario.setRol(rol);
        } else {
            usuario.setRol(null);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + id));
        usuarioRepository.delete(usuario);
    }

    public UsuarioDTO toDTO(Usuario u) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEstado(u.getEstado());
        dto.setPasswordNuncaExpira(u.getPasswordNuncaExpira());
        dto.setDebeCambiarPassword(u.getDebeCambiarPassword());
        dto.setPasswordExpiraEl(u.getPasswordExpiraEl());
        if (u.getRol() != null) {
            dto.setRolId(u.getRol().getId());
            dto.setRolNombre(u.getRol().getNombreRol());
        }
        return dto;
    }
}
